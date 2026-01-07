package com.apple.spark.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

public class VersionInfo_toString_6_0_Test_testToStringWithDefaultNullFields {

    // Utility to set private field values via reflection
    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    // Utility to get private field values via reflection
    private static Object getPrivateField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    // Utility to invoke toString via reflection (even though it's public, this ensures reflection invocation works)
    private static String invokeToStringReflectively(Object target) throws Exception {
        Method m = target.getClass().getDeclaredMethod("toString");
        m.setAccessible(true);
        return (String) m.invoke(target);
    }

    @Test
    public void testToStringWithDefaultNullFields() throws Exception {
        VersionInfo vi = new VersionInfo();
        // By default fields are null
        assertNull(getPrivateField(vi, "version"));
        assertNull(getPrivateField(vi, "gitCommit"));
        assertNull(getPrivateField(vi, "buildTime"));
        // Direct call
        String expected = "version: null, gitCommit: null, buildTime: null";
        assertEquals(expected, vi.toString());
        // Reflection invocation should produce the same result
        assertEquals(expected, invokeToStringReflectively(vi));
    }




}
