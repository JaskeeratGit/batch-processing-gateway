package com.apple.spark.util;

import java.lang.reflect.Method;
import java.util.Objects;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

public class KubernetesClusterAndNamespace_hashCode_3_0_Test_testHashCodeDifferentForDifferentMasterUrl {




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


}
