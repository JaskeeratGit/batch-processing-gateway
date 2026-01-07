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

class KubernetesClusterAndNamespace_toString_4_0_Test {

    @Test
    void testToString_withNonNullValues() {
        KubernetesClusterAndNamespace k = new KubernetesClusterAndNamespace("https://k8s.example", "default");
        String expected = "KubernetesClusterAndNamespace{masterUrl='https://k8s.example', namespace='default'}";
        assertEquals(expected, k.toString());
    }

    @Test
    void testToString_withNullValues() {
        KubernetesClusterAndNamespace k = new KubernetesClusterAndNamespace(null, null);
        String expected = "KubernetesClusterAndNamespace{masterUrl='null', namespace='null'}";
        assertEquals(expected, k.toString());
    }

    @Test
    void testToString_withOneNull() {
        KubernetesClusterAndNamespace k = new KubernetesClusterAndNamespace(null, "ns");
        String expected = "KubernetesClusterAndNamespace{masterUrl='null', namespace='ns'}";
        assertEquals(expected, k.toString());
    }

    @Test
    void testToString_afterReflectionFieldMutation() throws Exception {
        KubernetesClusterAndNamespace k = new KubernetesClusterAndNamespace("initialUrl", "initialNs");
        // Use reflection to change private final fields masterUrl and namespace
        setFinalField(k, "masterUrl", "mutatedUrl");
        setFinalField(k, "namespace", "mutatedNs");
        String expected = "KubernetesClusterAndNamespace{masterUrl='mutatedUrl', namespace='mutatedNs'}";
        assertEquals(expected, k.toString());
    }

    @Test
    void testToString_withEmptyAndSpecialChars_afterReflection() throws Exception {
        KubernetesClusterAndNamespace k = new KubernetesClusterAndNamespace("u", "n");
        setFinalField(k, "masterUrl", "");
        setFinalField(k, "namespace", "name-with-'/\"-chars\\");
        String expected = "KubernetesClusterAndNamespace{masterUrl='', namespace='name-with-'/\"-chars\\'}";
        // Because the toString implementation wraps values in single quotes and appends braces,
        // we assert that the produced string matches the exact expected representation.
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
