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

class ExceptionUtils_getExceptionNameAndMessage_0_0_Test_formatsRuntimeExceptionWithNullMessage {





    @Test
    void formatsRuntimeExceptionWithNullMessage() {
        RuntimeException rex = new RuntimeException((String) null);
        // getMessage() returns null, String.format will produce "class: null"
        String expected = rex.getClass().getName() + ": " + rex.getMessage();
        assertEquals(expected, ExceptionUtils.getExceptionNameAndMessage(rex));
    }

}
