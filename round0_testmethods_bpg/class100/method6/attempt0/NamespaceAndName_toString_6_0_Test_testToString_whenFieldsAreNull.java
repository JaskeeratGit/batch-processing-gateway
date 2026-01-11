package com.apple.spark.core;

import java.lang.reflect.Field;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Objects;

public class NamespaceAndName_toString_6_0_Test_testToString_whenFieldsAreNull {

    private void setPrivateFields(NamespaceAndName target, String namespace, String name) throws Exception {
        Field nsField = NamespaceAndName.class.getDeclaredField("namespace");
        nsField.setAccessible(true);
        nsField.set(target, namespace);
        Field nameField = NamespaceAndName.class.getDeclaredField("name");
        nameField.setAccessible(true);
        nameField.set(target, name);
    }

    private String expectedString(String namespace, String name) {
        return "{" + "namespace='" + namespace + '\'' + ", name='" + name + '\'' + '}';
    }

    @Test
    public void testToString_whenFieldsAreNull() throws Exception {
        NamespaceAndName obj = new NamespaceAndName();
        // ensure fields are null via reflection
        setPrivateFields(obj, null, null);
        String actual = obj.toString();
        String expected = expectedString(null, null);
        assertEquals(expected, actual);
    }




}
