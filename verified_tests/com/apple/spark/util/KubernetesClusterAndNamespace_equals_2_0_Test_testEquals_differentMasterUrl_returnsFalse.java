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
public class KubernetesClusterAndNamespace_equals_2_0_Test_testEquals_differentMasterUrl_returnsFalse {





    @Test
    public void testEquals_differentMasterUrl_returnsFalse() throws Exception {
        KubernetesClusterAndNamespace a = new KubernetesClusterAndNamespace("http://masterA", "ns1");
        KubernetesClusterAndNamespace b = new KubernetesClusterAndNamespace("http://masterB", "ns1");
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
        Method equalsMethod = KubernetesClusterAndNamespace.class.getMethod("equals", Object.class);
        assertFalse((Boolean) equalsMethod.invoke(a, b));
    }




}
