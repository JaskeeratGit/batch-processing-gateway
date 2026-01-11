package com.apple.spark.util;

import java.lang.reflect.Method;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.apple.spark.core.Constants;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;

class ExceptionUtils_getExceptionNameAndMessage_0_0_Test_returnsEmptyStringForNullReflective {


    @Test
    void returnsEmptyStringForNullReflective() throws Exception {
        Method m = ExceptionUtils.class.getDeclaredMethod("getExceptionNameAndMessage", Throwable.class);
        m.setAccessible(true);
        Object result = m.invoke(null, new Object[] { null });
        assertTrue(result instanceof String);
        assertEquals("", result);
    }




}
