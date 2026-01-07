package com.apple.spark.core;

import org.junit.jupiter.api.io.TempDir;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import com.apple.spark.core.KubernetesHelper;
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

/*
  Fixed: use sun.misc.Unsafe via reflection to modify static final fields
  instead of relying on the 'modifiers' field which may not exist on some JDKs.
*/
public class KubernetesHelper_tryGetServiceAccountToken_2_0_Test {

    private static void setFinalStatic(Class<?> clazz, String fieldName, String newValue) throws Exception {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        // obtain Unsafe instance via reflection
        Field unsafeField = Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        Object unsafe = unsafeField.get(null);
        // get base and offset for the static field
        Object staticFieldBase = (Object) unsafe.getClass().getMethod("staticFieldBase", Field.class).invoke(unsafe, field);
        long staticFieldOffset = (long) unsafe.getClass().getMethod("staticFieldOffset", Field.class).invoke(unsafe, field);
        // write new value
        unsafe.getClass().getMethod("putObject", Object.class, long.class, Object.class).invoke(unsafe, staticFieldBase, staticFieldOffset, newValue);
    }

    private static String getStaticField(Class<?> clazz, String fieldName) throws Exception {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        // obtain Unsafe instance via reflection
        Field unsafeField = Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        Object unsafe = unsafeField.get(null);
        Object staticFieldBase = (Object) unsafe.getClass().getMethod("staticFieldBase", Field.class).invoke(unsafe, field);
        long staticFieldOffset = (long) unsafe.getClass().getMethod("staticFieldOffset", Field.class).invoke(unsafe, field);
        return (String) unsafe.getClass().getMethod("getObject", Object.class, long.class).invoke(unsafe, staticFieldBase, staticFieldOffset);
    }

    @Test
    void testTryGetServiceAccountToken_success(@TempDir Path tempDir) throws Exception {
        String expectedToken = "my-test-token";
        // prepare token file in temp dir
        Path tokenFile = tempDir.resolve(KubernetesHelper.SERVICE_ACCOUNT_TOKEN_FILE);
        Files.write(tokenFile, expectedToken.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        // save originals
        String originalFolder = getStaticField(KubernetesHelper.class, "SERVICE_ACCOUNT_FOLDER");
        String originalTokenFile = getStaticField(KubernetesHelper.class, "SERVICE_ACCOUNT_TOKEN_FILE");
        try {
            // point the helper to our temp dir and token file name (token file name remains same but set anyway)
            setFinalStatic(KubernetesHelper.class, "SERVICE_ACCOUNT_FOLDER", tempDir.toString());
            setFinalStatic(KubernetesHelper.class, "SERVICE_ACCOUNT_TOKEN_FILE", tokenFile.getFileName().toString());
            String actual = KubernetesHelper.tryGetServiceAccountToken();
            assertEquals(expectedToken, actual);
        } finally {
            // restore originals
            setFinalStatic(KubernetesHelper.class, "SERVICE_ACCOUNT_FOLDER", originalFolder);
            setFinalStatic(KubernetesHelper.class, "SERVICE_ACCOUNT_TOKEN_FILE", originalTokenFile);
        }
    }

    @Test
    void testTryGetServiceAccountToken_missingFile_returnsEmpty(@TempDir Path tempDir) throws Exception {
        // ensure no token file exists in this temp dir
        // save originals
        String originalFolder = getStaticField(KubernetesHelper.class, "SERVICE_ACCOUNT_FOLDER");
        String originalTokenFile = getStaticField(KubernetesHelper.class, "SERVICE_ACCOUNT_TOKEN_FILE");
        try {
            setFinalStatic(KubernetesHelper.class, "SERVICE_ACCOUNT_FOLDER", tempDir.toString());
            setFinalStatic(KubernetesHelper.class, "SERVICE_ACCOUNT_TOKEN_FILE", "nonexistent-token-file");
            String actual = KubernetesHelper.tryGetServiceAccountToken();
            assertEquals("", actual);
        } finally {
            // restore originals
            setFinalStatic(KubernetesHelper.class, "SERVICE_ACCOUNT_FOLDER", originalFolder);
            setFinalStatic(KubernetesHelper.class, "SERVICE_ACCOUNT_TOKEN_FILE", originalTokenFile);
        }
    }
}
