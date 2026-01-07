package com.apple.spark.util;

import com.codahale.metrics.Meter;
import java.lang.reflect.Field;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.apple.spark.core.Constants;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;

public class ExceptionUtils_meterJsonProcessingException_2_0_Test {

    @Test
    void meterJsonProcessingException_incrementsMeterCount() throws Exception {
        Field jsonMeterField = ExceptionUtils.class.getDeclaredField("jsonProcessingExceptionMeter");
        jsonMeterField.setAccessible(true);
        Object meterObj = jsonMeterField.get(null);
        assertNotNull(meterObj, "jsonProcessingExceptionMeter should not be null for this test environment");
        assertTrue(meterObj instanceof Meter, "jsonProcessingExceptionMeter must be a com.codahale.metrics.Meter");
        Meter meter = (Meter) meterObj;
        long before = meter.getCount();
        // Call the method under test
        ExceptionUtils.meterJsonProcessingException();
        long after = meter.getCount();
        assertEquals(before + 1, after, "Calling meterJsonProcessingException should increment the meter count by 1");
    }

    @Test
    void meterJsonProcessingException_multipleCalls_incrementMultipleTimes() throws Exception {
        Field jsonMeterField = ExceptionUtils.class.getDeclaredField("jsonProcessingExceptionMeter");
        jsonMeterField.setAccessible(true);
        Object meterObj = jsonMeterField.get(null);
        assertNotNull(meterObj, "jsonProcessingExceptionMeter should not be null for this test environment");
        assertTrue(meterObj instanceof Meter, "jsonProcessingExceptionMeter must be a com.codahale.metrics.Meter");
        Meter meter = (Meter) meterObj;
        long before = meter.getCount();
        // Call the method under test multiple times
        ExceptionUtils.meterJsonProcessingException();
        ExceptionUtils.meterJsonProcessingException();
        ExceptionUtils.meterJsonProcessingException();
        long after = meter.getCount();
        assertEquals(before + 3, after, "Three calls to meterJsonProcessingException should increment the meter count by 3");
    }
}
