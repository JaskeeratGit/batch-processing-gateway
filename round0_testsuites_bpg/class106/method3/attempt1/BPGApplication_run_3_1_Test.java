package com.apple.spark;

import static org.mockito.ArgumentMatchers.*;
import com.apple.spark.AppConfig;
import com.apple.spark.BPGApplication;
import com.apple.spark.health.BPGHealthCheck;
import com.apple.spark.rest.ApplicationGetLogRest;
import com.apple.spark.rest.ApplicationSubmissionRest;
import com.apple.spark.core.Constants;
import com.apple.spark.core.BPGStatsdConfig;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Environment;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import java.lang.reflect.Method;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static com.apple.spark.core.Constants.QUEUE_INFO;
import static com.apple.spark.core.Constants.SERVICE_ABBR;
import com.apple.spark.core.ApplicationMonitor;
import com.apple.spark.core.ThrowableExceptionMapper;
import com.apple.spark.rest.AdminRest;
import com.apple.spark.rest.HealthcheckRest;
import com.apple.spark.rest.CloudStorageRest;
import com.apple.spark.security.User;
import com.apple.spark.security.UserNameAuthFilter;
import com.apple.spark.security.UserNameBasicAuthenticator;
import com.apple.spark.security.UserUnauthorizedHandler;
import com.apple.spark.util.CounterMetricContainer;
import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.auth.chained.ChainedAuthFilter;
import io.dropwizard.setup.Bootstrap;
import io.micrometer.core.instrument.Tag;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit tests for BPGApplication.run(AppConfig, Environment)
 *
 * These tests use Mockito to stub Environment interactions and reflection to invoke private methods.
 */
public class BPGApplication_run_3_1_Test {

    @BeforeEach
    public void setup() {
        // Ensure shared metric registries are cleared between tests
        SharedMetricRegistries.clear();
    }

    @Test
    public void testRun_whenApplicationContextPathIsNull_usesDefaultAndRegistersResources_monitorFalse() throws Exception {
        // Arrange
        AppConfig config = new AppConfig();
        // explicitly set null (default), to test branch where getApplicationContextPath() == null
        config.setApplicationContextPath(null);
        Environment env = mock(Environment.class);
        ServletContextHandler appContext = mock(ServletContextHandler.class);
        JerseyEnvironment jersey = mock(JerseyEnvironment.class);
        com.codahale.metrics.health.HealthCheckRegistry healthChecks = mock(com.codahale.metrics.health.HealthCheckRegistry.class);
        MetricRegistry metricRegistry = new MetricRegistry();
        when(env.getApplicationContext()).thenReturn(appContext);
        when(env.metrics()).thenReturn(metricRegistry);
        when(env.jersey()).thenReturn(jersey);
        when(env.healthChecks()).thenReturn(healthChecks);
        // monitorApplication = false
        BPGApplication app = new BPGApplication(false);
        // Act
        app.run(config, env);
        // Assert
        // application context path should be set to default constant
        verify(appContext).setContextPath(Constants.DEFAULT_APPLICATION_CONTEXT_PATH);
        // SharedMetricRegistries should contain the metric registry under DEFAULT_METRIC_REGISTRY
        MetricRegistry registered = SharedMetricRegistries.getOrCreate(Constants.DEFAULT_METRIC_REGISTRY);
        assertSame(metricRegistry, registered);
        // Jersey should have been asked to register multiple resources and features.
        // The run method registers 9 different things (5 rest resources + ThrowableExceptionMapper +
        // AuthDynamicFeature + AuthValueFactoryProvider.Binder + OpenApiResource config)
        // basic smoke check (at least majority)
        verify(jersey, atLeast(6)).register(any());
        // Specifically verify that ApplicationSubmissionRest and ApplicationGetLogRest got registered
        // by capturing arguments and checking their types.
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(jersey, atLeast(1)).register(captor.capture());
        boolean foundSubmission = captor.getAllValues().stream().anyMatch(o -> o != null && o.getClass().getSimpleName().equals("ApplicationSubmissionRest"));
        boolean foundGetLog = captor.getAllValues().stream().anyMatch(o -> o != null && o.getClass().getSimpleName().equals("ApplicationGetLogRest"));
        // At least one of the captured registrations should match those classes
        assertTrue(foundSubmission || foundGetLog);
        // Health check registration should be called with "sparkClusters" and a BPGHealthCheck instance
        verify(healthChecks).register(eq("sparkClusters"), any(BPGHealthCheck.class));
    }

    @Test
    public void testRun_withCustomContextPath_andMonitorTrue_andInvokePrivateSendPeriodicMetrics() throws Exception {
        // Arrange
        AppConfig config = new AppConfig();
        config.setApplicationContextPath("/custom");
        Environment env = mock(Environment.class);
        ServletContextHandler appContext = mock(ServletContextHandler.class);
        JerseyEnvironment jersey = mock(JerseyEnvironment.class);
        com.codahale.metrics.health.HealthCheckRegistry healthChecks = mock(com.codahale.metrics.health.HealthCheckRegistry.class);
        MetricRegistry metricRegistry = new MetricRegistry();
        when(env.getApplicationContext()).thenReturn(appContext);
        when(env.metrics()).thenReturn(metricRegistry);
        when(env.jersey()).thenReturn(jersey);
        when(env.healthChecks()).thenReturn(healthChecks);
        // Create application with monitoring enabled (monitorApplication = true)
        BPGApplication app = new BPGApplication(true);
        // Act
        app.run(config, env);
        // Assert
        verify(appContext).setContextPath("/custom");
        // Verify health check registered
        verify(healthChecks).register(eq("sparkClusters"), any(BPGHealthCheck.class));
        // Verify OpenApi registration was attempted (one of the registrations should be an OpenApiResource or its config)
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(jersey, atLeast(1)).register(captor.capture());
        boolean foundOpenApi = captor.getAllValues().stream().anyMatch(o -> o instanceof OpenApiResource || (o != null && o.getClass().getSimpleName().equals("OpenApiResource")));
        assertTrue(foundOpenApi);
        // Now use reflection to invoke private sendPeriodicMetrics to cover that branch as required.
        Method sendPeriodicMetrics = BPGApplication.class.getDeclaredMethod("sendPeriodicMetrics", AppConfig.class, MeterRegistry.class);
        sendPeriodicMetrics.setAccessible(true);
        // Use the same MeterRegistry creation method as the application
        MeterRegistry meterRegistry = BPGStatsdConfig.createMeterRegistry();
        // Invocation should complete without throwing
        sendPeriodicMetrics.invoke(app, config, meterRegistry);
    }
}
