package com.apple.spark.util;

import com.codahale.metrics.Meter;
import java.lang.reflect.Field;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Fixed unit test for ExceptionUtils.meterRuntimeException()
 *
 * This version avoids attempting to replace the private static final meter field
 * (which can lead to IllegalAccessException on modern JVMs). Instead it reads
 * the existing meter instance, snapshots its count, invokes the method under
 * test twice, and verifies the meter's count increased by two.
 */
public class ExceptionUtils_meterRuntimeException_5_0_Test_meterRuntimeException_marksMeter_multipleTimes {

    private Field runtimeExceptionMeterField;
    private Meter runtimeExceptionMeter;

    @BeforeEach
    void setUp() throws Exception {
        // Access the private static field (do not attempt to remove final modifier)
        runtimeExceptionMeterField = ExceptionUtils.class.getDeclaredField("runtimeExceptionMeter");
        runtimeExceptionMeterField.setAccessible(true);
        runtimeExceptionMeter = (Meter) runtimeExceptionMeterField.get(null);
    }

    @Test
    void meterRuntimeException_marksMeter_multipleTimes() {
        long before = runtimeExceptionMeter.getCount();
        ExceptionUtils.meterRuntimeException();
        ExceptionUtils.meterRuntimeException();
        long after = runtimeExceptionMeter.getCount();
        assertEquals(2L, after - before, "meterRuntimeException called twice should increment meter twice");
    }
}
