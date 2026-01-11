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
public class KubernetesClusterAndNamespace_equals_2_0_Test_testEquals_nullMasterUrlHandling {







    @Test
    public void testEquals_nullMasterUrlHandling() throws Exception {
        KubernetesClusterAndNamespace a = new KubernetesClusterAndNamespace(null, "ns1");
        KubernetesClusterAndNamespace b = new KubernetesClusterAndNamespace(null, "ns1");
        KubernetesClusterAndNamespace c = new KubernetesClusterAndNamespace("http://master", "ns1");
        // null masterUrl equals null masterUrl
        assertTrue(a.equals(b));
        assertTrue((Boolean) KubernetesClusterAndNamespace.class.getMethod("equals", Object.class).invoke(a, b));
        // null vs non-null -> false
        assertFalse(a.equals(c));
        assertFalse((Boolean) KubernetesClusterAndNamespace.class.getMethod("equals", Object.class).invoke(a, c));
    }


}
