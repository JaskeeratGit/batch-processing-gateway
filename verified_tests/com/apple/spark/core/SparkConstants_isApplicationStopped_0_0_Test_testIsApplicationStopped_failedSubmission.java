package com.apple.spark.core;

import java.lang.reflect.Method;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

class SparkConstants_isApplicationStopped_0_0_Test_testIsApplicationStopped_failedSubmission {

    private static Method isApplicationStoppedMethod;

    @BeforeAll
    static void setUp() throws Exception {
        isApplicationStoppedMethod = SparkConstants.class.getDeclaredMethod("isApplicationStopped", String.class);
        isApplicationStoppedMethod.setAccessible(true);
    }




    @Test
    void testIsApplicationStopped_failedSubmission() throws Exception {
        // Ensure first and second checks are false and third is true
        boolean result = (Boolean) isApplicationStoppedMethod.invoke(null, SparkConstants.FAILED_SUBMISSION_STATE);
        assertTrue(result, "FAILED_SUBMISSION should be considered stopped");
    }


}
