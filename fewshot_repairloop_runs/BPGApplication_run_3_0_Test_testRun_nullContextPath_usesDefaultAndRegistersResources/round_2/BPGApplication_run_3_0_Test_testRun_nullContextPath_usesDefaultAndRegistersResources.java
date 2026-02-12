package com.apple.spark;

import com.apple.spark.core.BPGStatsdConfig;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Environment;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
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
import com.apple.spark.core.Constants;
import com.apple.spark.core.ThrowableExceptionMapper;
import com.apple.spark.health.BPGHealthCheck;
import com.apple.spark.rest.AdminRest;
import com.apple.spark.rest.ApplicationGetLogRest;
import com.apple.spark.rest.ApplicationSubmissionRest;
import com.apple.spark.rest.HealthcheckRest;
import com.apple.spark.rest.CloudStorageRest;
import com.apple.spark.security.User;
import com.apple.spark.security.UserNameAuthFilter;
import com.apple.spark.security.UserNameBasicAuthenticator;
import com.apple.spark.security.UserUnauthorizedHandler;
import com.apple.spark.util.CounterMetricContainer;
import com.codahale.metrics.SharedMetricRegistries;
import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.auth.chained.ChainedAuthFilter;
import io.dropwizard.setup.Bootstrap;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.dropwizard.jetty.MutableServletContextHandler;

public class BPGApplication_run_3_0_Test_testRun_nullContextPath_usesDefaultAndRegistersResources {

    private Environment environment;

    private JerseyEnvironment jerseyEnvironment;

    private MutableServletContextHandler servletContextHandler;

    private HealthCheckRegistry healthCheckRegistry;

    private MetricRegistry metricRegistry;

    @BeforeEach
    public void setUp() {
        // Ensure SharedMetricRegistries has a default name to prevent IllegalState in test
        SharedMetricRegistries.setDefault(com.apple.spark.core.Constants.DEFAULT_METRIC_REGISTRY);

        environment = mock(Environment.class);
        jerseyEnvironment = mock(JerseyEnvironment.class);
        servletContextHandler = mock(MutableServletContextHandler.class);
        healthCheckRegistry = mock(HealthCheckRegistry.class);
        metricRegistry = new MetricRegistry();
        when(environment.jersey()).thenReturn(jerseyEnvironment);
        when(environment.getApplicationContext()).thenReturn(servletContextHandler);
        when(environment.metrics()).thenReturn(metricRegistry);
        when(environment.healthChecks()).thenReturn(healthCheckRegistry);
        // Allow registering anything without side effects
        doNothing().when(jerseyEnvironment).register(Mockito.any());
    }

    @Test
    public void testRun_nullContextPath_usesDefaultAndRegistersResources() {
        AppConfig config = new AppConfig();
        // explicit set to null to exercise null branch
        config.setApplicationContextPath(null);
        config.setAllowedUsers(Arrays.asList("allowed"));
        config.setBlockedUsers(Arrays.asList("blocked"));
        config.setSparkClusters(null);
        BPGApplication app = new BPGApplication(false);
        app.run(config, environment);
        // Verify context path set to default when null
        ArgumentCaptor<String> contextCaptor = ArgumentCaptor.forClass(String.class);
        verify(servletContextHandler).setContextPath(contextCaptor.capture());
        String contextPathSet = contextCaptor.getValue();
        assertNotNull(contextPathSet);
        assertFalse(contextPathSet.isEmpty());
        assertEquals(com.apple.spark.core.Constants.DEFAULT_APPLICATION_CONTEXT_PATH, contextPathSet);
        // Verify metrics() was called to retrieve application's MetricRegistry
        verify(environment).metrics();
        // Verify resources registration (some expected calls)
        verify(jerseyEnvironment, atLeast(1)).register(Mockito.any());
        // Verify healthcheck registration called with "sparkClusters"
        verify(healthCheckRegistry).register(eq("sparkClusters"), Mockito.any());
    }

}
