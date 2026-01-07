package com.apple.spark.core;

import com.apple.spark.operator.ApplicationState;
import com.apple.spark.operator.SparkApplication;
import com.apple.spark.operator.SparkApplicationStatus;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

class SparkApplicationResourceHelper_getState_0_0_Test_testGetStateWithNullSparkApplicationThrowsNullPointer {





    @Test
    void testGetStateWithNullSparkApplicationThrowsNullPointer() throws Exception {
        Method method = SparkApplicationResourceHelper.class.getDeclaredMethod("getState", SparkApplication.class);
        method.setAccessible(true);
        InvocationTargetException ex = assertThrows(InvocationTargetException.class, () -> method.invoke(null, (Object) null));
        assertNotNull(ex.getCause());
        assertTrue(ex.getCause() instanceof NullPointerException);
    }
}
