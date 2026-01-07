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

public class SparkSpecHelper_copyEnv_0_0_Test_testCopyEnv_SourceWithDuplicateNames_LastOneWins {

    private static void invokeCopyEnv(List<EnvVar> source, List<EnvVar> destination) throws Exception {
        Method copyEnv = SparkSpecHelper.class.getDeclaredMethod("copyEnv", List.class, List.class);
        copyEnv.setAccessible(true);
        copyEnv.invoke(null, source, destination);
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

}
