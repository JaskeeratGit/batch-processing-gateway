package com.apple.spark.core;

import java.lang.reflect.Method;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

class SparkConstants_isApplicationStopped_0_0_Test_testIsApplicationStopped_completedExact {

    private static Method isApplicationStoppedMethod;

    @BeforeAll
    static void setUp() throws Exception {
        isApplicationStoppedMethod = SparkConstants.class.getDeclaredMethod("isApplicationStopped", String.class);
        isApplicationStoppedMethod.setAccessible(true);
    }

    @Test
    void testIsApplicationStopped_completedExact() throws Exception {
        boolean result = (Boolean) isApplicationStoppedMethod.invoke(null, SparkConstants.COMPLETED_STATE);
        assertTrue(result, "COMPLETED should be considered stopped");
    }





}
