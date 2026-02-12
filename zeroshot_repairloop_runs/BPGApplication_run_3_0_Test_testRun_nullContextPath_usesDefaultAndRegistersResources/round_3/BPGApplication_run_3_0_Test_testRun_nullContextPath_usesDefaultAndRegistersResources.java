package com.apple.spark;

import com.apple.spark.core.Constants;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.health.HealthCheckRegistry;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Environment;
import io.dropwizard.jetty.MutableServletContextHandler;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class BPGApplication_run_3_0_Test_testRun_nullContextPath_usesDefaultAndRegistersResources {

    private Environment environment;

    private JerseyEnvironment jerseyEnvironment;

    private MutableServletContextHandler servletContextHandler;

    private HealthCheckRegistry healthCheckRegistry;

    private MetricRegistry metricRegistry;

    @BeforeEach
    public void setUp() {
        // Ensure any previous shared registry under the default name is removed so add(...) won't fail
        try {
            SharedMetricRegistries.remove(Constants.DEFAULT_METRIC_REGISTRY);
        } catch (Exception e) {
            // ignore if not present or removal fails for any reason
        }

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
        assertEquals(Constants.DEFAULT_APPLICATION_CONTEXT_PATH, contextPathSet);
        // Verify environment.metrics() was accessed (registry obtained from environment)
        verify(environment, atLeastOnce()).metrics();
        // Verify resources registration (some expected calls)
        verify(jerseyEnvironment, atLeast(1)).register(Mockito.any());
        // Verify healthcheck registration called with "sparkClusters"
        verify(healthCheckRegistry).register(eq("sparkClusters"), Mockito.any());
    }

}
