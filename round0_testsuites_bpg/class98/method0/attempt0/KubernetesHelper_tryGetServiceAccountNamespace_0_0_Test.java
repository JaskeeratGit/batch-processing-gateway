package com.apple.spark.core;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
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

@ExtendWith(MockitoExtension.class)
public class KubernetesHelper_tryGetServiceAccountNamespace_0_0_Test {

    private String originalServiceAccountFolder;

    @BeforeEach
    public void backupConstants() throws Exception {
        // Backup the original final static field value
        Field f = KubernetesHelper.class.getDeclaredField("SERVICE_ACCOUNT_FOLDER");
        f.setAccessible(true);
        originalServiceAccountFolder = (String) f.get(null);
    }

    @AfterEach
    public void restoreConstants() throws Exception {
        // Restore the final static field value
        setFinalStatic(KubernetesHelper.class.getDeclaredField("SERVICE_ACCOUNT_FOLDER"), originalServiceAccountFolder);
    }

    private static void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);
        // remove final modifier from field
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        // set the new value
        field.set(null, newValue);
    }

    @Test
    public void testReturnsNamespaceFromConfig() {
        try (MockedConstruction<ConfigBuilder> mocked = Mockito.mockConstruction(ConfigBuilder.class, (mock, context) -> {
            when(mock.getNamespace()).thenReturn("config-namespace");
        })) {
            String result = KubernetesHelper.tryGetServiceAccountNamespace();
            assertEquals("config-namespace", result);
        }
    }

    @Test
    public void testReturnsNamespaceFromFileWhenConfigEmpty() throws Exception {
        // Mock ConfigBuilder to return empty namespace so file is read
        try (MockedConstruction<ConfigBuilder> mocked = Mockito.mockConstruction(ConfigBuilder.class, (mock, context) -> {
            when(mock.getNamespace()).thenReturn("");
        })) {
            Path tempDir = Files.createTempDirectory("sa-test-");
            try {
                Path namespaceFile = tempDir.resolve(KubernetesHelper.SERVICE_ACCOUNT_NAMESPACE_FILE);
                Files.writeString(namespaceFile, "file-namespace");
                // Point KubernetesHelper to the temp directory by changing the static final field
                setFinalStatic(KubernetesHelper.class.getDeclaredField("SERVICE_ACCOUNT_FOLDER"), tempDir.toString());
                String result = KubernetesHelper.tryGetServiceAccountNamespace();
                assertEquals("file-namespace", result);
            } finally {
                // Cleanup
                try {
                    Files.deleteIfExists(tempDir.resolve(KubernetesHelper.SERVICE_ACCOUNT_NAMESPACE_FILE));
                    Files.deleteIfExists(tempDir);
                } catch (IOException ignore) {
                }
            }
        }
    }

    @Test
    public void testReturnsEmptyWhenFileReadThrows() throws Exception {
        // Mock ConfigBuilder to return null so file is attempted and will fail
        try (MockedConstruction<ConfigBuilder> mocked = Mockito.mockConstruction(ConfigBuilder.class, (mock, context) -> {
            when(mock.getNamespace()).thenReturn(null);
        })) {
            // Point to a non-existent directory to cause Files.readString to throw
            String nonExistent = "/this/path/should/not/exist/for/test";
            setFinalStatic(KubernetesHelper.class.getDeclaredField("SERVICE_ACCOUNT_FOLDER"), nonExistent);
            String result = KubernetesHelper.tryGetServiceAccountNamespace();
            assertEquals("", result);
        }
    }
}
