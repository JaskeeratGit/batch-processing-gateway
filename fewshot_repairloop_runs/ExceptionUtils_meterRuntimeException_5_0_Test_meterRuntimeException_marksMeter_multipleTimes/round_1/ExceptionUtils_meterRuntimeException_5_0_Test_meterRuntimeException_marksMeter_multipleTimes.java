package com.apple.spark.util;

import com.codahale.metrics.Meter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class ExceptionUtils_meterRuntimeException_5_0_Test_meterRuntimeException_marksMeter_multipleTimes {

    private Field runtimeExceptionMeterField;

    private Object originalRuntimeExceptionMeter;

    @BeforeEach
    void setUp() throws Exception {
        // Access the private static final field (read-only)
        runtimeExceptionMeterField = ExceptionUtils.class.getDeclaredField("runtimeExceptionMeter");
        runtimeExceptionMeterField.setAccessible(true);
        // Keep original to reference counts, but do not attempt to replace the final field
        originalRuntimeExceptionMeter = runtimeExceptionMeterField.get(null);
    }

    @AfterEach
    void tearDown() throws Exception {
        // Nothing to restore since we did not modify the final field
    }

    private long getMeterCount(Meter meter) {
        try {
            Method m = meter.getClass().getMethod("getCount");
            Object val = m.invoke(meter);
            if (val instanceof Number) {
                return ((Number) val).longValue();
            }
            throw new RuntimeException("Unexpected return type from getCount");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Meter does not have getCount method", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void meterRuntimeException_marksMeter_multipleTimes() throws Exception {
        Meter meter = (Meter) runtimeExceptionMeterField.get(null);
        long before = getMeterCount(meter);
        ExceptionUtils.meterRuntimeException();
        ExceptionUtils.meterRuntimeException();
        long after = getMeterCount(meter);
        assertEquals(2L, after - before, "meterRuntimeException called twice should increment meter twice");
    }
}
