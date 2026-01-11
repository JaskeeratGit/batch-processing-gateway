package com.apple.spark.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

public class VersionInfo_toString_6_0_Test_testToStringWithLongStrings {

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
    public void testToStringWithLongStrings() throws Exception {
        VersionInfo vi = new VersionInfo();
        String longVersion = "v".repeat(2000);
        String longGit = "g".repeat(2000);
        String longBuild = "b".repeat(2000);
        setPrivateField(vi, "version", longVersion);
        setPrivateField(vi, "gitCommit", longGit);
        setPrivateField(vi, "buildTime", longBuild);
        String expected = "version: " + longVersion + ", gitCommit: " + longGit + ", buildTime: " + longBuild;
        assertEquals(expected, vi.toString());
        assertEquals(expected, invokeToStringReflectively(vi));
    }
}
