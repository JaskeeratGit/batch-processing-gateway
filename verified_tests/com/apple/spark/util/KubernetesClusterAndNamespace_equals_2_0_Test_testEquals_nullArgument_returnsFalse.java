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
public class KubernetesClusterAndNamespace_equals_2_0_Test_testEquals_nullArgument_returnsFalse {


    @Test
    public void testEquals_nullArgument_returnsFalse() throws Exception {
        KubernetesClusterAndNamespace a = new KubernetesClusterAndNamespace("http://master", "ns1");
        // direct
        assertFalse(a.equals(null));
        // reflective
        Method equalsMethod = KubernetesClusterAndNamespace.class.getMethod("equals", Object.class);
        Object result = equalsMethod.invoke(a, new Object[] { null });
        assertFalse((Boolean) result);
    }







}
