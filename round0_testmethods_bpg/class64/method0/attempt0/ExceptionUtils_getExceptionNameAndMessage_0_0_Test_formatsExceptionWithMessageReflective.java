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

class ExceptionUtils_getExceptionNameAndMessage_0_0_Test_formatsExceptionWithMessageReflective {




    @Test
    void formatsExceptionWithMessageReflective() throws Exception {
        Exception ex = new Exception("boom");
        Method m = ExceptionUtils.class.getDeclaredMethod("getExceptionNameAndMessage", Throwable.class);
        m.setAccessible(true);
        String result = (String) m.invoke(null, new Object[] { ex });
        String expected = ex.getClass().getName() + ": " + ex.getMessage();
        assertEquals(expected, result);
    }


}
