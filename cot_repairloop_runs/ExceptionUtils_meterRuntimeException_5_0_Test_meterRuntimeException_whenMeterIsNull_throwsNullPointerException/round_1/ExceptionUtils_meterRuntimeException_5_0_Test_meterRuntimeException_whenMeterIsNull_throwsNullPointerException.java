package com.apple.spark.util;

import com.codahale.metrics.Meter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ExceptionUtils.meterRuntimeException()
 */
public class ExceptionUtils_meterRuntimeException_5_0_Test_meterRuntimeException_whenMeterIsNull_throwsNullPointerException {

    private Field runtimeExceptionMeterField;
    private Object originalRuntimeExceptionMeter;
    private Field modifiersField;

    @BeforeEach
    void setUp() throws Exception {
        // Access the private static final field
        runtimeExceptionMeterField = ExceptionUtils.class.getDeclaredField("runtimeExceptionMeter");
        runtimeExceptionMeterField.setAccessible(true);
        // Keep original to restore later
        originalRuntimeExceptionMeter = runtimeExceptionMeterField.get(null);
        // Remove final modifier so we can set the field
        modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(runtimeExceptionMeterField, runtimeExceptionMeterField.getModifiers() & ~Modifier.FINAL);
    }

    @AfterEach
    void tearDown() throws Exception {
        // Restore original value (most important)
        runtimeExceptionMeterField.set(null, originalRuntimeExceptionMeter);
    }

    // A simple Meter subclass that counts mark invocations
    static class CountingMeter extends Meter {

        private final AtomicInteger count = new AtomicInteger(0);

        @Override
        public void mark() {
            count.incrementAndGet();
        }

        @Override
        public void mark(long n) {
            // Meter.mark(long) accepts long; keep simple counting semantics
            count.addAndGet((int) n);
        }

        // Must match Metered.getCount() which returns long
        public long getCount() {
            return count.get();
        }
    }

    @Test
    void meterRuntimeException_whenMeterIsNull_throwsNullPointerException() throws Exception {
        // Set the metric field to null to exercise behavior when field is null
        runtimeExceptionMeterField.set(null, null);
        assertThrows(NullPointerException.class,
            () -> ExceptionUtils.meterRuntimeException(),
            "Calling meterRuntimeException when the meter is null should throw NPE");
    }

    @Test
    void meterRuntimeException_marksMeter_whenMeterIsPresent() throws Exception {
        // Set a counting meter and ensure meterRuntimeException invokes mark()
        CountingMeter cm = new CountingMeter();
        runtimeExceptionMeterField.set(null, cm);

        // call method under test
        ExceptionUtils.meterRuntimeException();

        // verify the meter was marked exactly once
        assertEquals(1L, cm.getCount(), "meterRuntimeException should call mark() once on the meter");
    }
}
