package com.apple.spark.core;

import org.slf4j.Logger;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.mockito.ArgumentMatchers.*;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.apple.spark.AppConfig;
import com.apple.spark.util.EndAwareInputStream;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.*;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import org.slf4j.LoggerFactory;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * JUnit 5 tests for KubernetesHelper.closeQuietly(Closeable)
 */
public class KubernetesHelper_closeQuietly_7_0_Test_testCloseQuietly_nullArgument_logs {

    private Logger originalLogger;

    private Logger mockLogger;

    @BeforeEach
    public void setUp() throws Exception {
        // Replace the private static final logger with a Mockito mock so we can verify logging behavior
        Field loggerField = KubernetesHelper.class.getDeclaredField("logger");
        loggerField.setAccessible(true);
        // Save original logger to restore later
        originalLogger = (Logger) loggerField.get(null);
        // Remove final modifier on the logger field so we can set it
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(loggerField, loggerField.getModifiers() & ~Modifier.FINAL);
        // Set mock logger
        mockLogger = mock(Logger.class);
        loggerField.set(null, mockLogger);
    }

    @AfterEach
    public void tearDown() throws Exception {
        // Restore original logger
        Field loggerField = KubernetesHelper.class.getDeclaredField("logger");
        loggerField.setAccessible(true);
        // Remove final modifier (in case it's still set)
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(loggerField, loggerField.getModifiers() & ~Modifier.FINAL);
        loggerField.set(null, originalLogger);
    }




    @Test
    public void testCloseQuietly_nullArgument_logs() {
        // Passing null should not propagate a NullPointerException; it should be caught and logged
        Assertions.assertDoesNotThrow(() -> KubernetesHelper.closeQuietly(null));
        // message should indicate failure to close null
        verify(mockLogger).warn(eq("Failed to close null"), any(Throwable.class));
    }
}
