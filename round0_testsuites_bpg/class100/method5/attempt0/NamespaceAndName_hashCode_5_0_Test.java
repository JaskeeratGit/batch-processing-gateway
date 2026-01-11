package com.apple.spark.core;

import java.lang.reflect.Field;
import java.util.Objects;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

class NamespaceAndName_hashCode_5_0_Test {

    @Test
    void testHashCode_bothNull_defaultConstructor() {
        NamespaceAndName nn = new NamespaceAndName();
        int expected = Objects.hash(null, null);
        assertEquals(expected, nn.hashCode());
    }

    @Test
    void testHashCode_parameterizedConstructor_nonNulls() {
        NamespaceAndName nn = new NamespaceAndName("namespace1", "name1");
        int expected = Objects.hash("namespace1", "name1");
        assertEquals(expected, nn.hashCode());
    }

    @Test
    void testHashCode_viaSetters_oneNull_otherNonNull() {
        NamespaceAndName nn = new NamespaceAndName();
        nn.setNamespace(null);
        nn.setName("onlyName");
        int expected = Objects.hash(null, "onlyName");
        assertEquals(expected, nn.hashCode());
        nn.setNamespace("onlyNamespace");
        nn.setName(null);
        expected = Objects.hash("onlyNamespace", null);
        assertEquals(expected, nn.hashCode());
    }

    @Test
    void testHashCode_twoInstancesSameValues_haveSameHash() {
        NamespaceAndName a = new NamespaceAndName("ns", "nm");
        NamespaceAndName b = new NamespaceAndName();
        b.setNamespace("ns");
        b.setName("nm");
        assertEquals(a.hashCode(), b.hashCode());
        assertEquals(Objects.hash("ns", "nm"), a.hashCode());
    }

    @Test
    void testHashCode_differentValues_expectedDifferentHash() {
        NamespaceAndName a = new NamespaceAndName("nsA", "nmA");
        NamespaceAndName b = new NamespaceAndName("nsB", "nmB");
        int expectedA = Objects.hash("nsA", "nmA");
        int expectedB = Objects.hash("nsB", "nmB");
        assertEquals(expectedA, a.hashCode());
        assertEquals(expectedB, b.hashCode());
        assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testHashCode_reflectionModifyPrivateFields() throws Exception {
        NamespaceAndName nn = new NamespaceAndName();
        Class<?> cls = nn.getClass();
        Field nsField = cls.getDeclaredField("namespace");
        Field nameField = cls.getDeclaredField("name");
        nsField.setAccessible(true);
        nameField.setAccessible(true);
        // set private fields directly using reflection
        nsField.set(nn, "reflectNS");
        nameField.set(nn, "reflectName");
        int expected = Objects.hash("reflectNS", "reflectName");
        assertEquals(expected, nn.hashCode());
        // change fields again via reflection to null and verify hash
        nsField.set(nn, null);
        nameField.set(nn, "onlyReflectName");
        expected = Objects.hash(null, "onlyReflectName");
        assertEquals(expected, nn.hashCode());
    }
}
