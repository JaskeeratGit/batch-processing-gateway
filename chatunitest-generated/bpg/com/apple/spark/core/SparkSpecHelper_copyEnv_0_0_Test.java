package com.apple.spark.core;

import com.apple.spark.operator.EnvVar;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.HashMap;
import java.util.Map;

public class SparkSpecHelper_copyEnv_0_0_Test {

    private static void invokeCopyEnv(List<EnvVar> source, List<EnvVar> destination) throws Exception {
        Method copyEnv = SparkSpecHelper.class.getDeclaredMethod("copyEnv", List.class, List.class);
        copyEnv.setAccessible(true);
        copyEnv.invoke(null, source, destination);
    }

    @Test
    public void testCopyEnv_AddsWhenDestinationEmpty() throws Exception {
        EnvVar a = new EnvVar("A", "1");
        EnvVar b = new EnvVar("B", "2");
        List<EnvVar> source = new ArrayList<>(Arrays.asList(a, b));
        List<EnvVar> destination = new ArrayList<>();
        invokeCopyEnv(source, destination);
        assertEquals(2, destination.size(), "Destination should have two elements after copy");
        // Should preserve the same object references from source
        assertSame(a, destination.get(0));
        assertSame(b, destination.get(1));
    }

    @Test
    public void testCopyEnv_ReplacesExistingByIndex() throws Exception {
        EnvVar oldA = new EnvVar("A", "old");
        EnvVar c = new EnvVar("C", "c");
        List<EnvVar> destination = new ArrayList<>(Arrays.asList(oldA, c));
        EnvVar newA = new EnvVar("A", "new");
        List<EnvVar> source = new ArrayList<>(Arrays.asList(newA));
        invokeCopyEnv(source, destination);
        assertEquals(2, destination.size(), "Destination size should remain the same when replacing");
        // The A at index 0 should be replaced by the source instance
        assertSame(newA, destination.get(0));
        // Other entries should remain untouched
        assertSame(c, destination.get(1));
    }

    @Test
    public void testCopyEnv_WhenDestinationHasDuplicateNames_ReplacesLastIndex() throws Exception {
        EnvVar a1 = new EnvVar("A", "v1");
        EnvVar b = new EnvVar("B", "b");
        // same name as a1, occurs later
        EnvVar a2 = new EnvVar("A", "v2");
        List<EnvVar> destination = new ArrayList<>(Arrays.asList(a1, b, a2));
        EnvVar aNew = new EnvVar("A", "new");
        List<EnvVar> source = new ArrayList<>(Arrays.asList(aNew));
        invokeCopyEnv(source, destination);
        // Map in method should map "A" -> last index (2), so replacement should occur at index 2
        assertSame(a1, destination.get(0), "First occurrence of duplicate name should remain");
        assertSame(b, destination.get(1), "Other entries should remain unchanged");
        assertSame(aNew, destination.get(2), "Last occurrence should be replaced by source instance");
    }

    @Test
    public void testCopyEnv_SourceWithDuplicateNames_LastOneWins() throws Exception {
        EnvVar oldX = new EnvVar("X", "old");
        List<EnvVar> destination = new ArrayList<>(Arrays.asList(oldX));
        EnvVar x1 = new EnvVar("X", "s1");
        // later in source list
        EnvVar x2 = new EnvVar("X", "s2");
        List<EnvVar> source = new ArrayList<>(Arrays.asList(x1, x2));
        invokeCopyEnv(source, destination);
        // The final value for "X" should be the last one in source (x2)
        assertEquals(1, destination.size());
        assertSame(x2, destination.get(0));
        assertNotSame(x1, destination.get(0));
    }

    @Test
    public void testCopyEnv_SourceEmpty_DestinationUnchanged() throws Exception {
        EnvVar d = new EnvVar("D", "d");
        List<EnvVar> destination = new ArrayList<>(Arrays.asList(d));
        List<EnvVar> source = new ArrayList<>();
        invokeCopyEnv(source, destination);
        // Nothing should be added or removed
        assertEquals(1, destination.size());
        assertSame(d, destination.get(0));
    }
}
