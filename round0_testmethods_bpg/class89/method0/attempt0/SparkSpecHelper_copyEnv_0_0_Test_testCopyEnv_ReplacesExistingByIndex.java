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

public class SparkSpecHelper_copyEnv_0_0_Test_testCopyEnv_ReplacesExistingByIndex {

    private static void invokeCopyEnv(List<EnvVar> source, List<EnvVar> destination) throws Exception {
        Method copyEnv = SparkSpecHelper.class.getDeclaredMethod("copyEnv", List.class, List.class);
        copyEnv.setAccessible(true);
        copyEnv.invoke(null, source, destination);
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



}
