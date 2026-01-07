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

public class SparkSpecHelper_copyEnv_0_0_Test_testCopyEnv_WhenDestinationHasDuplicateNames_ReplacesLastIndex {

    private static void invokeCopyEnv(List<EnvVar> source, List<EnvVar> destination) throws Exception {
        Method copyEnv = SparkSpecHelper.class.getDeclaredMethod("copyEnv", List.class, List.class);
        copyEnv.setAccessible(true);
        copyEnv.invoke(null, source, destination);
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


}
