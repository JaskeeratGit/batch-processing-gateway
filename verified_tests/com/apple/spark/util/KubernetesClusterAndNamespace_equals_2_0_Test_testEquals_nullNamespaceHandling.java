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
public class KubernetesClusterAndNamespace_equals_2_0_Test_testEquals_nullNamespaceHandling {








    @Test
    public void testEquals_nullNamespaceHandling() throws Exception {
        KubernetesClusterAndNamespace a = new KubernetesClusterAndNamespace("http://master", null);
        KubernetesClusterAndNamespace b = new KubernetesClusterAndNamespace("http://master", null);
        KubernetesClusterAndNamespace c = new KubernetesClusterAndNamespace("http://master", "ns1");
        // null namespace equals null namespace
        assertTrue(a.equals(b));
        assertTrue((Boolean) KubernetesClusterAndNamespace.class.getMethod("equals", Object.class).invoke(a, b));
        // null vs non-null -> false
        assertFalse(a.equals(c));
        assertFalse((Boolean) KubernetesClusterAndNamespace.class.getMethod("equals", Object.class).invoke(a, c));
    }

}
