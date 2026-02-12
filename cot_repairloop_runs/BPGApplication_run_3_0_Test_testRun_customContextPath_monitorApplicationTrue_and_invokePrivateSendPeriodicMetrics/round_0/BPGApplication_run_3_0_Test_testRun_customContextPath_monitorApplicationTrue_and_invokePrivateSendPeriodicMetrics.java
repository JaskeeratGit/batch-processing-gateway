package com.apple.spark;

import com.apple.spark.core.BPGStatsdConfig;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Environment;
import java.lang.reflect.Method;
import java.util.Collections;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import io.dropwizard.jetty.MutableServletContextHandler;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * Fixed unit test for BPGApplication.run(...) when a custom context path is provided and
 * monitorApplication is true. The primary fix is to return the correct type from
 * environment.getApplicationContext(): io.dropwizard.jetty.MutableServletContextHandler.
 */
@ExtendWith(MockitoExtension.class)
public class BPGApplication_run_3_0_Test_testRun_customContextPath_monitorApplicationTrue_and_invokePrivateSendPeriodicMetrics {

    private Environment environment;

    private JerseyEnvironment jerseyEnvironment;

    private MutableServletContextHandler servletContextHandler;

    private HealthCheckRegistry healthCheckRegistry;

    private MetricRegistry metricRegistry;

    @BeforeEach
    public void setUp() {
        environment = mock(Environment.class);
        jerseyEnvironment = mock(JerseyEnvironment.class);
        // Use the Dropwizard MutableServletContextHandler (the type returned by Environment.getApplicationContext())
        servletContextHandler = mock(MutableServletContextHandler.class);
        healthCheckRegistry = mock(HealthCheckRegistry.class);
        metricRegistry = new MetricRegistry();
        when(environment.jersey()).thenReturn(jerseyEnvironment);
        when(environment.getApplicationContext()).thenReturn(servletContextHandler);
        when(environment.metrics()).thenReturn(metricRegistry);
        when(environment.healthChecks()).thenReturn(healthCheckRegistry);
        // Allow registering anything without side effects
        doNothing().when(jerseyEnvironment).register(any(Object.class));
    }


    @Test
    public void testRun_customContextPath_monitorApplicationTrue_and_invokePrivateSendPeriodicMetrics() throws Exception {
        AppConfig config = new AppConfig();
        // setApplicationContextPath and other setters are expected to exist on AppConfig
        config.setApplicationContextPath("/custom");
        config.setAllowedUsers(Collections.emptyList());
        config.setBlockedUsers(Collections.emptyList());
        // Keep sparkClusters null to avoid heavy informer logic in ApplicationMonitor.start
        config.setSparkClusters(null);
        // Create application with monitoring enabled to exercise monitorApplication branch
        BPGApplication app = new BPGApplication(true);
        // Run should not throw even when monitorApplication true (daemon threads may be started)
        app.run(config, environment);
        // Verify context path set to provided value
        verify(servletContextHandler).setContextPath("/custom");
        // Also verify that Jersey registers components at least once
        verify(jerseyEnvironment, atLeast(1)).register(any(Object.class));
        // Now use reflection to invoke the private sendPeriodicMetrics method directly
        Method sendPeriodicMetrics = BPGApplication.class.getDeclaredMethod("sendPeriodicMetrics", AppConfig.class, MeterRegistry.class);
        sendPeriodicMetrics.setAccessible(true);
        // Use createMeterRegistry to get a MeterRegistry instance
        MeterRegistry meterRegistry = BPGStatsdConfig.createMeterRegistry();
        // Invoke the private method; it should schedule a Timer task and not throw
        sendPeriodicMetrics.invoke(app, config, meterRegistry);
        // If we reached here, the private method executed without throwing
        assertTrue(true);
    }
}
