package com.apple.spark.core;

import java.lang.reflect.Method;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Objects;

public class NamespaceAndName_equals_4_0_Test {

    // Helper subclass to test the getClass() != o.getClass() branch
    static class SubNamespaceAndName extends NamespaceAndName {

        public SubNamespaceAndName(String namespace, String name) {
            super(namespace, name);
        }
    }

    @Test
    public void testEquals_sameInstance_reflectionInvoke() throws Exception {
        NamespaceAndName a = new NamespaceAndName("ns", "name");
        Method equalsMethod = NamespaceAndName.class.getDeclaredMethod("equals", Object.class);
        // invoke equals reflectively with the same instance -> should hit "this == o" branch and return true
        Boolean result = (Boolean) equalsMethod.invoke(a, a);
        assertTrue(result);
    }

    @Test
    public void testEquals_nullArgument() throws Exception {
        NamespaceAndName a = new NamespaceAndName("ns", "name");
        // direct call
        assertFalse(a.equals(null));
        // reflective invocation with explicit null argument
        Method equalsMethod = NamespaceAndName.class.getDeclaredMethod("equals", Object.class);
        Boolean result = (Boolean) equalsMethod.invoke(a, new Object[] { null });
        assertFalse(result);
    }

    @Test
    public void testEquals_differentClass() {
        NamespaceAndName a = new NamespaceAndName("ns", "name");
        NamespaceAndName sub = new SubNamespaceAndName("ns", "name");
        // even though fields match, getClass() != o.getClass() should make equals return false
        assertFalse(a.equals(sub));
        assertFalse(sub.equals(a));
    }

    @Test
    public void testEquals_bothFieldsEqual() {
        NamespaceAndName a = new NamespaceAndName("ns", "name");
        NamespaceAndName b = new NamespaceAndName("ns", "name");
        assertTrue(a.equals(b));
        // symmetry
        assertTrue(b.equals(a));
    }

    @Test
    public void testEquals_differentNamespace() {
        NamespaceAndName a = new NamespaceAndName("ns1", "name");
        NamespaceAndName b = new NamespaceAndName("ns2", "name");
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
    }

    @Test
    public void testEquals_differentName() {
        NamespaceAndName a = new NamespaceAndName("ns", "name1");
        NamespaceAndName b = new NamespaceAndName("ns", "name2");
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
    }

    @Test
    public void testEquals_bothNullFields() {
        // default constructor leaves namespace and name as null
        NamespaceAndName a = new NamespaceAndName();
        NamespaceAndName b = new NamespaceAndName();
        // Objects.equals(null, null) should be true for both fields -> overall true
        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
        // also equals to self
        assertTrue(a.equals(a));
    }

    @Test
    public void testEquals_oneNullNamespace() {
        NamespaceAndName a = new NamespaceAndName(null, "name");
        NamespaceAndName b = new NamespaceAndName("ns", "name");
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
    }

    @Test
    public void testEquals_oneNullName() {
        NamespaceAndName a = new NamespaceAndName("ns", null);
        NamespaceAndName b = new NamespaceAndName("ns", "name");
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
    }
}
