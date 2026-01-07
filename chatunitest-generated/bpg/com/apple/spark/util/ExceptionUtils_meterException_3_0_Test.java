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

/**
 * JUnit 5 tests for ExceptionUtils.meterException().
 */
public class ExceptionUtils_meterException_3_0_Test {

    private Object originalExceptionMeter;

    @BeforeEach
    public void setUp() throws Exception {
        Field f = ExceptionUtils.class.getDeclaredField("exceptionMeter");
        f.setAccessible(true);
        originalExceptionMeter = f.get(null);
    }

    @Test
    public void testMeterException_marksOnce() throws Exception {
        Field f = ExceptionUtils.class.getDeclaredField("exceptionMeter");
        f.setAccessible(true);
        Meter meter = (Meter) f.get(null);
        long before = meter.getCount();
        // Call the focal method
        ExceptionUtils.meterException();
        long after = meter.getCount();
        // Verify mark was called exactly once
        assertEquals(before + 1L, after);
    }

    @Test
    public void testMeterException_multipleCalls_accumulatesMarks() throws Exception {
        Field f = ExceptionUtils.class.getDeclaredField("exceptionMeter");
        f.setAccessible(true);
        Meter meter = (Meter) f.get(null);
        long before = meter.getCount();
        // Call multiple times
        ExceptionUtils.meterException();
        ExceptionUtils.meterException();
        ExceptionUtils.meterException();
        long after = meter.getCount();
        // Verify mark was called three times
        assertEquals(before + 3L, after);
    }
}
