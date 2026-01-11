package com.apple.spark.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

public class VersionInfo_toString_6_0_Test_testToStringWithSetters {

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
    public void testToStringWithSetters() {
        VersionInfo vi = new VersionInfo();
        vi.setVersion("1.2.3");
        vi.setGitCommit("abcdef");
        vi.setBuildTime("2025-01-01T00:00:00Z");
        String expected = "version: 1.2.3, gitCommit: abcdef, buildTime: 2025-01-01T00:00:00Z";
        // direct call
        assertEquals(expected, vi.toString());
        // getters should reflect the values set
        assertEquals("1.2.3", vi.getVersion());
        assertEquals("abcdef", vi.getGitCommit());
        assertEquals("2025-01-01T00:00:00Z", vi.getBuildTime());
    }



}
