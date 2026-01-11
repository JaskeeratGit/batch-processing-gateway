package com.apple.spark.util;

import java.lang.reflect.Method;
import java.util.Objects;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

public class KubernetesClusterAndNamespace_hashCode_3_0_Test_testHashCodeDifferentForDifferentNamespace {





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

}
