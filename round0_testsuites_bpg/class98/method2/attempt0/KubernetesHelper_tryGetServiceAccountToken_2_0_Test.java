package com.apple.spark.core;

import org.junit.jupiter.api.io.TempDir;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
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
import java.nio.file.Paths;

public class KubernetesHelper_tryGetServiceAccountToken_2_0_Test {

    private String originalServiceAccountFolder;

    private String originalTokenFile;

    @BeforeEach
    void storeOriginalConstants() throws Exception {
        // Store originals to restore after each test
        Field folderField = KubernetesHelper.class.getDeclaredField("SERVICE_ACCOUNT_FOLDER");
        folderField.setAccessible(true);
        originalServiceAccountFolder = (String) folderField.get(null);
        Field tokenField = KubernetesHelper.class.getDeclaredField("SERVICE_ACCOUNT_TOKEN_FILE");
        tokenField.setAccessible(true);
        originalTokenFile = (String) tokenField.get(null);
    }

    @AfterEach
    void restoreOriginalConstants() throws Exception {
        // Restore original constant values
        setFinalStatic(KubernetesHelper.class, "SERVICE_ACCOUNT_FOLDER", originalServiceAccountFolder);
        setFinalStatic(KubernetesHelper.class, "SERVICE_ACCOUNT_TOKEN_FILE", originalTokenFile);
    }

    @Test
    void testTryGetServiceAccountToken_success(@TempDir Path tempDir) throws Exception {
        // Point SERVICE_ACCOUNT_FOLDER and SERVICE_ACCOUNT_TOKEN_FILE to a temporary location
        setFinalStatic(KubernetesHelper.class, "SERVICE_ACCOUNT_FOLDER", tempDir.toString());
        setFinalStatic(KubernetesHelper.class, "SERVICE_ACCOUNT_TOKEN_FILE", "token");
        Path tokenPath = tempDir.resolve("token");
        String expectedToken = "my-test-token";
        Files.writeString(tokenPath, expectedToken);
        // Invoke the focal method via reflection
        Method method = KubernetesHelper.class.getDeclaredMethod("tryGetServiceAccountToken");
        method.setAccessible(true);
        String actual = (String) method.invoke(null);
        assertEquals(expectedToken, actual);
    }

    @Test
    void testTryGetServiceAccountToken_missingFile_returnsEmpty(@TempDir Path tempDir) throws Exception {
        // Point to a temporary directory but do not create the token file
        setFinalStatic(KubernetesHelper.class, "SERVICE_ACCOUNT_FOLDER", tempDir.toString());
        setFinalStatic(KubernetesHelper.class, "SERVICE_ACCOUNT_TOKEN_FILE", "token");
        Path tokenPath = tempDir.resolve("token");
        Files.deleteIfExists(tokenPath);
        // Invoke the focal method via reflection
        Method method = KubernetesHelper.class.getDeclaredMethod("tryGetServiceAccountToken");
        method.setAccessible(true);
        String actual = (String) method.invoke(null);
        // Expect empty string on any Throwable (file not found / unreadable)
        assertEquals("", actual);
    }

    // Utility to modify static final String fields via reflection for testing purposes
    private static void setFinalStatic(Class<?> clazz, String fieldName, String newValue) throws Exception {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        // Remove final modifier
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, newValue);
    }
}
