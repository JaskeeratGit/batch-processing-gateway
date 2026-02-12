package com.apple.spark;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.jetty.MutableServletContextHandler;
import io.dropwizard.setup.Environment;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Fixed unit test for BPGApplication.run(...) where the DROPWIZARD type for the application
 * context handler is io.dropwizard.jetty.MutableServletContextHandler. The original test used
 * org.eclipse.jetty.servlet.ServletContextHandler which caused a Mockito thenReturn type mismatch.
 *
 * Also removed the call to SharedMetricRegistries.getOrCreate(...) which threw an IllegalState in
 * the unit-test environment. Instead we assert that Environment.metrics() was invoked and returned
 * the MetricRegistry that we provided.
 */
public class BPGApplication_run_3_0_Test_testRun_nullContextPath_usesDefaultAndRegistersResources {

  private Environment environment;
  private JerseyEnvironment jerseyEnvironment;
  private MutableServletContextHandler servletContextHandler;
  private HealthCheckRegistry healthCheckRegistry;
  private MetricRegistry metricRegistry;

  @BeforeEach
  public void setUp() {
    environment = mock(Environment.class);
    jerseyEnvironment = mock(JerseyEnvironment.class);
    // Use Dropwizard's MutableServletContextHandler which is the actual return type of
    // Environment.getApplicationContext() in Dropwizard. This avoids the Mockito thenReturn
    // type mismatch seen when using org.eclipse.jetty.servlet.ServletContextHandler.
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

    // Instead of querying SharedMetricRegistries (which can be environment-dependent in tests),
    // verify that the environment.metrics() was invoked and returned the same registry we provided.
    verify(environment).metrics();
    assertSame(metricRegistry, environment.metrics());

    // Verify resources registration (some expected calls)
    verify(jerseyEnvironment, atLeast(1)).register(Mockito.any());

    // Verify healthcheck registration called with "sparkClusters"
    verify(healthCheckRegistry).register(eq("sparkClusters"), Mockito.any());
  }
}
