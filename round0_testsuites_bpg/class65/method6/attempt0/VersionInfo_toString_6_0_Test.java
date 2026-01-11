package com.apple.spark.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

public class VersionInfo_toString_6_0_Test {

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

    @Test
    public void testToStringWithEmptyStringsUsingReflection() throws Exception {
        VersionInfo vi = new VersionInfo();
        setPrivateField(vi, "version", "");
        setPrivateField(vi, "gitCommit", "");
        setPrivateField(vi, "buildTime", "");
        // Ensure fields were set to empty strings
        assertEquals("", getPrivateField(vi, "version"));
        assertEquals("", getPrivateField(vi, "gitCommit"));
        assertEquals("", getPrivateField(vi, "buildTime"));
        String expected = "version: , gitCommit: , buildTime: ";
        assertEquals(expected, vi.toString());
        // also via reflection
        assertEquals(expected, invokeToStringReflectively(vi));
    }

    @Test
    public void testToStringWithSpecialCharactersAndCommas() throws Exception {
        VersionInfo vi = new VersionInfo();
        setPrivateField(vi, "version", "v1.0,alpha");
        setPrivateField(vi, "gitCommit", "abc,def,123");
        setPrivateField(vi, "buildTime", "time,with,commas");
        String expected = "version: v1.0,alpha, gitCommit: abc,def,123, buildTime: time,with,commas";
        // direct call
        assertEquals(expected, vi.toString());
        // invoke reflectively as well
        assertEquals(expected, invokeToStringReflectively(vi));
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
