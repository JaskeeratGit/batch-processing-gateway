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

class SparkApplicationResourceHelper_getState_0_0_Test_testGetStateReturnsNullWhenStateFieldIsNull {




    @Test
    void testGetStateReturnsNullWhenStateFieldIsNull() throws Exception {
        // arrange
        SparkApplication app = new SparkApplication();
        SparkApplicationStatus status = new SparkApplicationStatus();
        ApplicationState applicationState = new ApplicationState();
        // explicit null state
        applicationState.setState(null);
        status.setApplicationState(applicationState);
        app.setStatus(status);
        Method method = SparkApplicationResourceHelper.class.getDeclaredMethod("getState", SparkApplication.class);
        method.setAccessible(true);
        // act
        Object result = method.invoke(null, app);
        // assert
        assertNull(result);
    }

}
