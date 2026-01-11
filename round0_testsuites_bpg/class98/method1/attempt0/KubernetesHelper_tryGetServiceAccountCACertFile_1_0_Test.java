package com.apple.spark.core;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.Closeable;
import java.io.InputStream;

public class KubernetesHelper_tryGetServiceAccountCACertFile_1_0_Test {

    @Test
    public void tryGetServiceAccountCACertFile_whenFileExists_returnsAbsolutePath() throws Exception {
        // Prepare the expected path the method will build
        Path expectedPath = Paths.get(KubernetesHelper.SERVICE_ACCOUNT_FOLDER, KubernetesHelper.SERVICE_ACCOUNT_CA_CERT_FILE).toAbsolutePath();
        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            // Make Files.exists(...) return true for the exact path constructed in the method
            filesMock.when(() -> Files.exists(expectedPath)).thenReturn(true);
            // Invoke the static method via reflection
            Class<?> cls = Class.forName("com.apple.spark.core.KubernetesHelper");
            Method method = cls.getDeclaredMethod("tryGetServiceAccountCACertFile");
            method.setAccessible(true);
            Object result = method.invoke(null);
            assertNotNull(result);
            assertTrue(result instanceof String);
            assertEquals(expectedPath.toString(), result);
            // Verify Files.exists was called with the expected path
            filesMock.verify(() -> Files.exists(expectedPath));
        }
    }

    @Test
    public void tryGetServiceAccountCACertFile_whenFileNotExists_returnsEmptyString() throws Exception {
        Path expectedPath = Paths.get(KubernetesHelper.SERVICE_ACCOUNT_FOLDER, KubernetesHelper.SERVICE_ACCOUNT_CA_CERT_FILE).toAbsolutePath();
        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            // Make Files.exists(...) return false for the exact path constructed in the method
            filesMock.when(() -> Files.exists(expectedPath)).thenReturn(false);
            // Invoke the static method via reflection
            Class<?> cls = Class.forName("com.apple.spark.core.KubernetesHelper");
            Method method = cls.getDeclaredMethod("tryGetServiceAccountCACertFile");
            method.setAccessible(true);
            Object result = method.invoke(null);
            assertNotNull(result);
            assertTrue(result instanceof String);
            assertEquals("", result);
            // Verify Files.exists was called with the expected path
            filesMock.verify(() -> Files.exists(expectedPath));
        }
    }
}
