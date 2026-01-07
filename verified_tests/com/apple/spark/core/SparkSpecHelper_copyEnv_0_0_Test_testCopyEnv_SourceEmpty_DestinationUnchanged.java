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

public class SparkSpecHelper_copyEnv_0_0_Test_testCopyEnv_SourceEmpty_DestinationUnchanged {

    private static void invokeCopyEnv(List<EnvVar> source, List<EnvVar> destination) throws Exception {
        Method copyEnv = SparkSpecHelper.class.getDeclaredMethod("copyEnv", List.class, List.class);
        copyEnv.setAccessible(true);
        copyEnv.invoke(null, source, destination);
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
