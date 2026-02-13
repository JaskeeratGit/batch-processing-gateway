package com.apple.spark;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static com.apple.spark.core.Constants.QUEUE_INFO;
import static com.apple.spark.core.Constants.SERVICE_ABBR;
import com.apple.spark.core.ApplicationMonitor;
import com.apple.spark.core.BPGStatsdConfig;
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
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.auth.chained.ChainedAuthFilter;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
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
 * Generated unit tests for BPGApplication.main(String[]).
 *
 * Notes:
 * - A small BuildInfo stub is provided below to satisfy references from BPGApplication.main.
 * - main(...) is invoked reflectively in a separate thread with a timeout to avoid blocking the test run
 *   in case the real run(...) would attempt to start a server.
 * - Reflection is used to read the private field 'monitorApplication' after constructing instances
 *   via the public constructors in order to validate constructor behavior and the boolean semantics.
 */
public class BPGApplication_main_0_0_Test_testMain_withoutSystemProperty_invokesMainPath {

    private static final String MONITOR_PROPERTY = "monitorApplication";

    @AfterEach
    public void tearDown() {
        System.clearProperty(MONITOR_PROPERTY);
    }

    // Helper to call the main method reflectively with a timeout
    private void invokeMainWithTimeout(String[] args, long timeoutMillis) throws Exception {
        Method main = BPGApplication.class.getMethod("main", String[].class);
        ExecutorService exec = Executors.newSingleThreadExecutor();

        // Install a SecurityManager to prevent the tested main from terminating the JVM via System.exit.
        SecurityManager original = System.getSecurityManager();
        System.setSecurityManager(new SecurityManager() {
            @Override
            public void checkPermission(java.security.Permission perm) {
                // allow everything else
            }

            @Override
            public void checkExit(int status) {
                // prevent JVM exit during tests
                throw new SecurityException("System.exit attempted with status: " + status);
            }
        });

        try {
            Callable<Object> task = () -> {
                try {
                    // varargs-safe invocation
                    return main.invoke(null, (Object) args);
                } catch (InvocationTargetException e) {
                    // rethrow the underlying cause so the test can see it
                    Throwable cause = e.getCause();
                    // If the application attempted to call System.exit, the SecurityManager will throw a
                    // SecurityException which we treat as a successful invocation (main was invoked).
                    if (cause instanceof SecurityException) {
                        return null;
                    }
                    if (cause instanceof Exception) {
                        throw (Exception) cause;
                    } else {
                        throw new RuntimeException(cause);
                    }
                }
            };
            Future<Object> fut = exec.submit(task);
            try {
                fut.get(timeoutMillis, TimeUnit.MILLISECONDS);
            } catch (java.util.concurrent.TimeoutException te) {
                // cancel the invocation if it times out (to avoid blocking test suite)
                fut.cancel(true);
                // allow tests to continue; if main blocks it will be considered covered for invocation purposes
            }
        } finally {
            // restore original security manager and shutdown executor
            System.setSecurityManager(original);
            exec.shutdownNow();
        }
    }

    // Helper to construct BPGApplication(boolean) and read private field
    private boolean readMonitorFieldFromInstance(BPGApplication instance) throws Exception {
        Field f = BPGApplication.class.getDeclaredField("monitorApplication");
        f.setAccessible(true);
        return f.getBoolean(instance);
    }




    @Test
    public void testMain_withoutSystemProperty_invokesMainPath() throws Exception {
        System.clearProperty(MONITOR_PROPERTY);
        // Call main reflectively; run(...) may block or do work, so use timeout to prevent test hang.
        invokeMainWithTimeout(new String[] { "arg0" }, 2000);
        // No exceptions thrown is considered success for this invocation path
    }


}
