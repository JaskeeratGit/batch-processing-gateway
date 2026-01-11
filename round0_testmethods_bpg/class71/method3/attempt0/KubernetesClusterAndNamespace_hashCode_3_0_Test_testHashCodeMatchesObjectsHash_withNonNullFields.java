package com.apple.spark.util;

import java.lang.reflect.Method;
import java.util.Objects;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

public class KubernetesClusterAndNamespace_hashCode_3_0_Test_testHashCodeMatchesObjectsHash_withNonNullFields {

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





}
