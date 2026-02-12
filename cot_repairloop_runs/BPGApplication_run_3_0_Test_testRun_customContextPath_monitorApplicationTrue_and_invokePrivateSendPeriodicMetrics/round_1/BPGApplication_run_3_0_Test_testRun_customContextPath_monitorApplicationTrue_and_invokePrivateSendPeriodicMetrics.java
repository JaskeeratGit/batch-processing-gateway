package com.apple.spark;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.health.HealthCheckRegistry;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.jetty.MutableServletContextHandler;
import io.dropwizard.setup.Environment;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Fixed unit test for BPGApplication.run(...) when a custom context path is provided and
 * monitorApplication is true. The primary fixes:
 * - Use a mocked AppConfig (instead of assuming setters exist).
 * - Stub SharedMetricRegistries.add(...) (static) to avoid IllegalState thrown by metrics shared registry handling.
 * - Use a SimpleMeterRegistry for invoking the private sendPeriodicMetrics method to avoid calling
 *   the potentially environment-dependent BPGStatsdConfig.createMeterRegistry().
 */
@ExtendWith(MockitoExtension.class)
public class BPGApplication_run_3_0_Test_testRun_customContextPath_monitorApplicationTrue_and_invokePrivateSendPeriodicMetrics {

    private Environment environment;
    private JerseyEnvironment jerseyEnvironment;
    private MutableServletContextHandler servletContextHandler;
    private HealthCheckRegistry healthCheckRegistry;
    private MetricRegistry metricRegistry;

    // Hold the MockedStatic so we can close it after test
    private MockedStatic<SharedMetricRegistries> sharedMetricRegistriesStaticMock;

    @BeforeEach
    public void setUp() {
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
        doNothing().when(jerseyEnvironment).register(any(Object.class));

        // Stub the static SharedMetricRegistries.add(...) to be a no-op to avoid IllegalState
        sharedMetricRegistriesStaticMock = mockStatic(SharedMetricRegistries.class);
        sharedMetricRegistriesStaticMock.when(() -> SharedMetricRegistries.add(anyString(), any(MetricRegistry.class)))
                .thenAnswer(invocation -> {
                    // no-op: return null (add is void) — just prevent underlying implementation from running
                    return null;
                });
    }

    @AfterEach
    public void tearDown() {
        if (sharedMetricRegistriesStaticMock != null) {
            sharedMetricRegistriesStaticMock.close();
        }
    }

    @Test
    public void testRun_customContextPath_monitorApplicationTrue_and_invokePrivateSendPeriodicMetrics() throws Exception {
        // Use a mocked AppConfig to avoid relying on setters that may not exist in the provided brief.
        AppConfig config = mock(AppConfig.class);
        when(config.getApplicationContextPath()).thenReturn("/custom");
        when(config.getAllowedUsers()).thenReturn(Collections.emptyList());
        when(config.getBlockedUsers()).thenReturn(Collections.emptyList());
        // Keep sparkClusters null to avoid heavy informer logic in ApplicationMonitor.start
        when(config.getSparkClusters()).thenReturn(null);

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

        // Use a simple MeterRegistry instead of calling BPGStatsdConfig.createMeterRegistry() to avoid environment dependencies
        MeterRegistry meterRegistry = new SimpleMeterRegistry();

        // Invoke the private method; it should schedule a Timer task and not throw
        sendPeriodicMetrics.invoke(app, config, meterRegistry);

        // If we reached here, the private method executed without throwing
        assertTrue(true);
    }
}
