package com.apple.spark.core;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.util.Comparator;
import java.lang.reflect.Modifier;
import sun.misc.Unsafe;
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

class KubernetesHelper_tryGetServiceAccountNamespace_0_0_Test {

    private String originalServiceAccountFolder;

    @BeforeEach
    void saveOriginal() throws Exception {
        Field f = KubernetesHelper.class.getField("SERVICE_ACCOUNT_FOLDER");
        f.setAccessible(true);
        originalServiceAccountFolder = (String) f.get(null);
    }

    @AfterEach
    void restoreOriginal() throws Exception {
        setServiceAccountFolder(originalServiceAccountFolder);
    }

    @Test
    void testReturnsNamespaceFromFile() throws Exception {
        Path tempDir = Files.createTempDirectory("sa-folder-");
        try {
            Path nsFile = tempDir.resolve("namespace");
            Files.writeString(nsFile, "file-namespace");
            setServiceAccountFolder(tempDir.toString());
            String result = KubernetesHelper.tryGetServiceAccountNamespace();
            assertEquals("file-namespace", result);
        } finally {
            deleteRecursively(tempDir);
        }
    }

    @Test
    void testReturnsEmptyWhenFileMissing() throws Exception {
        Path tempDir = Files.createTempDirectory("sa-folder-");
        try {
            // Do NOT create the namespace file
            setServiceAccountFolder(tempDir.toString());
            String result = KubernetesHelper.tryGetServiceAccountNamespace();
            assertEquals("", result);
        } finally {
            deleteRecursively(tempDir);
        }
    }

    @Test
    void testReturnsEmptyWhenFileIsDirectory() throws Exception {
        Path tempDir = Files.createTempDirectory("sa-folder-");
        try {
            // Create a directory named "namespace" to simulate the file being a directory
            Files.createDirectory(tempDir.resolve("namespace"));
            setServiceAccountFolder(tempDir.toString());
            String result = KubernetesHelper.tryGetServiceAccountNamespace();
            assertEquals("", result);
        } finally {
            deleteRecursively(tempDir);
        }
    }

    // Helper to change the public static final SERVICE_ACCOUNT_FOLDER field via reflection.
    // Uses VarHandle when possible, falls back to sun.misc.Unsafe if necessary.
    private void setServiceAccountFolder(String newValue) throws Exception {
        Field field = KubernetesHelper.class.getField("SERVICE_ACCOUNT_FOLDER");
        field.setAccessible(true);
        // Try VarHandle (Java 9+)
        try {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(KubernetesHelper.class, MethodHandles.lookup());
            VarHandle vh = lookup.findStaticVarHandle(KubernetesHelper.class, "SERVICE_ACCOUNT_FOLDER", String.class);
            vh.set(newValue);
            return;
        } catch (Throwable ignored) {
            // fallback to Unsafe
        }
        // Fallback: use Unsafe to write the static field
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            Unsafe unsafe = (Unsafe) unsafeField.get(null);
            Object staticBase = unsafe.staticFieldBase(field);
            long offset = unsafe.staticFieldOffset(field);
            unsafe.putObject(staticBase, offset, newValue);
        } catch (Throwable t) {
            // As a last resort, attempt removing final modifier (may not be available on some JVMs)
            try {
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
                field.set(null, newValue);
            } catch (Throwable e) {
                throw new IllegalStateException("Unable to set SERVICE_ACCOUNT_FOLDER via reflection", e);
            }
        }
    }

    // Recursive delete for temp directories
    private void deleteRecursively(Path path) {
        if (path == null)
            return;
        try {
            if (Files.exists(path)) {
                Files.walk(path).sorted(Comparator.reverseOrder()).forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException ignored) {
                    }
                });
            }
        } catch (IOException ignored) {
        }
    }
}
