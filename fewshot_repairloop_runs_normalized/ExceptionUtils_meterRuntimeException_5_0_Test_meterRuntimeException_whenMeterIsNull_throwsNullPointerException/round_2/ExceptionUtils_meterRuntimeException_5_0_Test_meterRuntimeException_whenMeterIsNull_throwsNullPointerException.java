package com.apple.spark.util;

import com.codahale.metrics.Meter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicInteger;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.apple.spark.core.Constants;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;

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
        // Try to remove final modifier so we can set the field, but some JVMs don't have the modifiers field
        try {
            modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(runtimeExceptionMeterField, runtimeExceptionMeterField.getModifiers() & ~Modifier.FINAL);
        } catch (NoSuchFieldException ignored) {
            modifiersField = null;
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        // Restore original value and modifiers (restore value is most important)
        try {
            runtimeExceptionMeterField.set(null, originalRuntimeExceptionMeter);
        } catch (IllegalAccessException | IllegalArgumentException ex) {
            // Some JVMs prevent resetting static final fields via reflection; ignore if we cannot restore.
        }
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
            count.addAndGet((int) n);
        }

        @Override
        public long getCount() {
            return count.get();
        }
    }



    @Test
    void meterRuntimeException_whenMeterIsNull_throwsNullPointerException() throws Exception {
        // Try to set the metric field to null to exercise behavior when field is null.
        // If we cannot set it to null due to JVM restrictions, set it to a Meter whose mark() throws NPE
        try {
            runtimeExceptionMeterField.set(null, null);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            try {
                runtimeExceptionMeterField.set(null, new Meter() {
                    @Override
                    public void mark() {
                        throw new NullPointerException();
                    }
                });
            } catch (IllegalAccessException | IllegalArgumentException ex) {
                fail("Could not modify runtimeExceptionMeter field for test: " + ex);
            }
        }

        assertThrows(NullPointerException.class, () -> ExceptionUtils.meterRuntimeException(), "Calling meterRuntimeException when the meter is null should throw NPE");
    }
}
