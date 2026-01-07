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

class ExceptionUtils_isTooManyRequest_1_0_Test_testDeepCauseChainWithNullMessages {





    @Test
    void testDeepCauseChainWithNullMessages() throws Exception {
        // intermediate messages are null, final cause has matching message
        Throwable deep = new Exception("too many request");
        Throwable mid = new Exception((String) null, deep);
        Throwable top = new Exception((String) null, mid);
        assertTrue(ExceptionUtils.isTooManyRequest(top));
        Method m = ExceptionUtils.class.getDeclaredMethod("isTooManyRequest", Throwable.class);
        m.setAccessible(true);
        Object result = m.invoke(null, top);
        assertEquals(Boolean.TRUE, result);
    }

}
