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
public class KubernetesClusterAndNamespace_equals_2_0_Test_testEquals_differentClass_returnsFalse {



    @Test
    public void testEquals_differentClass_returnsFalse() throws Exception {
        KubernetesClusterAndNamespace base = new KubernetesClusterAndNamespace("http://master", "ns1");
        // anonymous subclass - getClass() will differ
        KubernetesClusterAndNamespace subclassInstance = new KubernetesClusterAndNamespace("http://master", "ns1") {
        };
        // direct
        assertFalse(base.equals(subclassInstance));
        // symmetry for this branch
        assertFalse(subclassInstance.equals(base));
        // reflective both ways
        Method equalsMethod = KubernetesClusterAndNamespace.class.getMethod("equals", Object.class);
        assertFalse((Boolean) equalsMethod.invoke(base, subclassInstance));
        // invoking via subclass's class reflection as well
        Method equalsMethodOnSubclass = subclassInstance.getClass().getMethod("equals", Object.class);
        assertFalse((Boolean) equalsMethodOnSubclass.invoke(subclassInstance, base));
    }






}
