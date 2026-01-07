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

public class SparkSpecHelper_copyEnv_0_0_Test_testCopyEnv_AddsWhenDestinationEmpty {

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




}
