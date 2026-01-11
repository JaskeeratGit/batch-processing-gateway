package com.apple.spark.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Objects;

class KubernetesClusterAndNamespace_toString_4_0_Test_testToString_afterReflectionFieldMutation {




    @Test
    void testToString_afterReflectionFieldMutation() throws Exception {
        KubernetesClusterAndNamespace k = new KubernetesClusterAndNamespace("initialUrl", "initialNs");
        // Use reflection to change private final fields masterUrl and namespace
        setFinalField(k, "masterUrl", "mutatedUrl");
        setFinalField(k, "namespace", "mutatedNs");
        String expected = "KubernetesClusterAndNamespace{masterUrl='mutatedUrl', namespace='mutatedNs'}";
        assertEquals(expected, k.toString());
    }


    // Helper to set private final fields via reflection
    private static void setFinalField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        // Remove final modifier if present
        try {
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        } catch (NoSuchFieldException ignored) {
            // Some JVMs (e.g., newer) may not allow or need this; ignore if not present.
        }
        field.set(target, value);
    }
}
