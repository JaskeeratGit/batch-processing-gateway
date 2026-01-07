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
public class KubernetesHelper_tryGetServiceAccountNamespace_0_0_Test_testReturnsNamespaceFromConfig {

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


}
