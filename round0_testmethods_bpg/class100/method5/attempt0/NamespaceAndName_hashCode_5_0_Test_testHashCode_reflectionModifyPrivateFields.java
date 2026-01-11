package com.apple.spark.core;

import java.lang.reflect.Field;
import java.util.Objects;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

class NamespaceAndName_hashCode_5_0_Test_testHashCode_reflectionModifyPrivateFields {






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
