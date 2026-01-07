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
public class KubernetesClusterAndNamespace_equals_2_0_Test {

    @Test
    public void testEquals_sameReference_returnsTrue() throws Exception {
        KubernetesClusterAndNamespace a = new KubernetesClusterAndNamespace("http://master", "ns1");
        // direct call
        assertTrue(a.equals(a));
        // reflective invocation
        Method equalsMethod = KubernetesClusterAndNamespace.class.getMethod("equals", Object.class);
        Object result = equalsMethod.invoke(a, a);
        assertTrue((Boolean) result);
    }

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

    @Test
    public void testEquals_differentMasterUrl_returnsFalse() throws Exception {
        KubernetesClusterAndNamespace a = new KubernetesClusterAndNamespace("http://masterA", "ns1");
        KubernetesClusterAndNamespace b = new KubernetesClusterAndNamespace("http://masterB", "ns1");
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
        Method equalsMethod = KubernetesClusterAndNamespace.class.getMethod("equals", Object.class);
        assertFalse((Boolean) equalsMethod.invoke(a, b));
    }

    @Test
    public void testEquals_differentNamespace_returnsFalse() throws Exception {
        KubernetesClusterAndNamespace a = new KubernetesClusterAndNamespace("http://master", "nsA");
        KubernetesClusterAndNamespace b = new KubernetesClusterAndNamespace("http://master", "nsB");
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
        Method equalsMethod = KubernetesClusterAndNamespace.class.getMethod("equals", Object.class);
        assertFalse((Boolean) equalsMethod.invoke(a, b));
    }

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

    @Test
    public void testEquals_bothFieldsNull_returnsTrue() throws Exception {
        KubernetesClusterAndNamespace a = new KubernetesClusterAndNamespace(null, null);
        KubernetesClusterAndNamespace b = new KubernetesClusterAndNamespace(null, null);
        assertTrue(a.equals(b));
        assertTrue((Boolean) KubernetesClusterAndNamespace.class.getMethod("equals", Object.class).invoke(a, b));
    }
}
