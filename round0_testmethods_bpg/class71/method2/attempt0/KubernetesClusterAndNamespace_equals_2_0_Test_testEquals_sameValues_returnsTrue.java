package com.apple.spark.util;

import java.lang.reflect.Method;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Objects;

/**
 * JUnit 5 tests for KubernetesClusterAndNamespace.equals(Object)
 */
public class KubernetesClusterAndNamespace_equals_2_0_Test_testEquals_sameValues_returnsTrue {




    @Test
    public void testEquals_sameValues_returnsTrue() throws Exception {
        KubernetesClusterAndNamespace a = new KubernetesClusterAndNamespace("http://master", "ns1");
        KubernetesClusterAndNamespace b = new KubernetesClusterAndNamespace("http://master", "ns1");
        // direct
        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
        // reflective
        Method equalsMethod = KubernetesClusterAndNamespace.class.getMethod("equals", Object.class);
        assertTrue((Boolean) equalsMethod.invoke(a, b));
        assertTrue((Boolean) equalsMethod.invoke(b, a));
    }





}
