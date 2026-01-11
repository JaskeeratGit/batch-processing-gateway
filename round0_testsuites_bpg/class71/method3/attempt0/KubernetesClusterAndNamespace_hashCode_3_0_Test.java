package com.apple.spark.util;

import java.lang.reflect.Method;
import java.util.Objects;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

public class KubernetesClusterAndNamespace_hashCode_3_0_Test {

    @Test
    void testHashCodeMatchesObjectsHash_withNonNullFields() throws Exception {
        String masterUrl = "https://example.com";
        String namespace = "default";
        KubernetesClusterAndNamespace obj = new KubernetesClusterAndNamespace(masterUrl, namespace);
        int expected = Objects.hash(masterUrl, namespace);
        // direct call
        assertEquals(expected, obj.hashCode());
        // reflective invocation
        Method hashCodeMethod = KubernetesClusterAndNamespace.class.getDeclaredMethod("hashCode");
        hashCodeMethod.setAccessible(true);
        Object reflectedResult = hashCodeMethod.invoke(obj);
        assertTrue(reflectedResult instanceof Integer);
        assertEquals(expected, ((Integer) reflectedResult).intValue());
    }

    @Test
    void testHashCodeMatchesObjectsHash_withNullFields() throws Exception {
        String masterUrl = null;
        String namespace = null;
        KubernetesClusterAndNamespace obj = new KubernetesClusterAndNamespace(masterUrl, namespace);
        int expected = Objects.hash(masterUrl, namespace);
        assertEquals(expected, obj.hashCode());
        Method hashCodeMethod = KubernetesClusterAndNamespace.class.getDeclaredMethod("hashCode");
        hashCodeMethod.setAccessible(true);
        assertEquals(expected, ((Integer) hashCodeMethod.invoke(obj)).intValue());
    }

    @Test
    void testHashCodeConsistentAcrossCalls() {
        KubernetesClusterAndNamespace obj = new KubernetesClusterAndNamespace("u", "n");
        int first = obj.hashCode();
        int second = obj.hashCode();
        int third = obj.hashCode();
        assertEquals(first, second);
        assertEquals(first, third);
    }

    @Test
    void testHashCodeDifferentForDifferentMasterUrl() {
        KubernetesClusterAndNamespace a = new KubernetesClusterAndNamespace("url1", "ns");
        KubernetesClusterAndNamespace b = new KubernetesClusterAndNamespace("url2", "ns");
        // Compare against Objects.hash to avoid relying on collision assumptions
        int expectedA = Objects.hash("url1", "ns");
        int expectedB = Objects.hash("url2", "ns");
        assertEquals(expectedA, a.hashCode());
        assertEquals(expectedB, b.hashCode());
        // verify they are not equal (deterministic given Objects.hash with different inputs)
        assertNotEquals(expectedA, expectedB);
    }

    @Test
    void testHashCodeDifferentForDifferentNamespace() {
        KubernetesClusterAndNamespace a = new KubernetesClusterAndNamespace("url", "ns1");
        KubernetesClusterAndNamespace b = new KubernetesClusterAndNamespace("url", "ns2");
        int expectedA = Objects.hash("url", "ns1");
        int expectedB = Objects.hash("url", "ns2");
        assertEquals(expectedA, a.hashCode());
        assertEquals(expectedB, b.hashCode());
        assertNotEquals(expectedA, expectedB);
    }

    @Test
    void testHashCodeVariousCombinations_matchObjectsHash() throws Exception {
        String[][] combos = { { "u1", "n1" }, { "u1", null }, { null, "n1" }, { null, null }, { "", "" }, { "u-long-value", "n-long-value" } };
        Method hashCodeMethod = KubernetesClusterAndNamespace.class.getDeclaredMethod("hashCode");
        hashCodeMethod.setAccessible(true);
        for (String[] combo : combos) {
            String u = combo[0];
            String n = combo[1];
            KubernetesClusterAndNamespace obj = new KubernetesClusterAndNamespace(u, n);
            int expected = Objects.hash(u, n);
            assertEquals(expected, obj.hashCode());
            assertEquals(expected, ((Integer) hashCodeMethod.invoke(obj)).intValue());
        }
    }
}
