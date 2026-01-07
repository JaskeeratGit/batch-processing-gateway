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

class ExceptionUtils_isTooManyRequest_1_0_Test_testMessageContainsPhraseReturnsTrue {


    @Test
    void testMessageContainsPhraseReturnsTrue() throws Exception {
        Throwable t = new RuntimeException("There are too many request errors right now");
        assertTrue(ExceptionUtils.isTooManyRequest(t));
        Method m = ExceptionUtils.class.getDeclaredMethod("isTooManyRequest", Throwable.class);
        m.setAccessible(true);
        Object result = m.invoke(null, t);
        assertEquals(Boolean.TRUE, result);
    }




}
