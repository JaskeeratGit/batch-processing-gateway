package com.apple.spark.core;

import java.lang.reflect.Method;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

class SparkConstants_isApplicationStopped_0_0_Test_testIsApplicationStopped_notStopped {

    private static Method isApplicationStoppedMethod;

    @BeforeAll
    static void setUp() throws Exception {
        isApplicationStoppedMethod = SparkConstants.class.getDeclaredMethod("isApplicationStopped", String.class);
        isApplicationStoppedMethod.setAccessible(true);
    }





    @Test
    void testIsApplicationStopped_notStopped() throws Exception {
        boolean result = (Boolean) isApplicationStoppedMethod.invoke(null, SparkConstants.RUNNING_STATE);
        assertFalse(result, "RUNNING should not be considered stopped");
        boolean result2 = (Boolean) isApplicationStoppedMethod.invoke(null, SparkConstants.SUBMITTED_STATE);
        assertFalse(result2, "SUBMITTED should not be considered stopped");
        boolean result3 = (Boolean) isApplicationStoppedMethod.invoke(null, "some-other-state");
        assertFalse(result3, "An unrelated state should not be considered stopped");
    }

}
