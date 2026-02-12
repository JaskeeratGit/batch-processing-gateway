package com.apple.spark.util;

import com.codahale.metrics.Meter;
import java.lang.reflect.Field;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Fixed unit test for ExceptionUtils.meterRuntimeException().
 *
 * Original test attempted to replace a private static final field via reflection,
 * which is not reliable on modern JVMs and caused IllegalAccessException / timeouts.
 *
 * This test instead reads the existing Meter instance from the private static field,
 * observes its count before and after calling the method under test, and asserts
 * that the count increased by one.
 */
public class ExceptionUtils_meterRuntimeException_5_0_Test_meterRuntimeException_marksMeter_once {

    private Field runtimeExceptionMeterField;
    private Meter runtimeExceptionMeterInstance;

    @BeforeEach
    void setUp() throws Exception {
        // Access the private static final field but DO NOT attempt to replace it.
        runtimeExceptionMeterField = ExceptionUtils.class.getDeclaredField("runtimeExceptionMeter");
        runtimeExceptionMeterField.setAccessible(true);
        Object value = runtimeExceptionMeterField.get(null);
        assertNotNull(value, "runtimeExceptionMeter field should be initialized");
        assertTrue(value instanceof Meter, "runtimeExceptionMeter should be a Meter");
        runtimeExceptionMeterInstance = (Meter) value;
    }

    @AfterEach
    void tearDown() {
        // No replacement or restoration required; we did not modify the field.
        runtimeExceptionMeterInstance = null;
        runtimeExceptionMeterField = null;
    }

    @Test
    void meterRuntimeException_marksMeter_once() {
        long before = runtimeExceptionMeterInstance.getCount();
        ExceptionUtils.meterRuntimeException();
        long after = runtimeExceptionMeterInstance.getCount();
        assertEquals(before + 1L, after, "meterRuntimeException should mark the meter once");
    }
}
