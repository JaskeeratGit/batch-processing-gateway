package com.apple.spark.util;

import com.codahale.metrics.Meter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import com.codahale.metrics.MetricRegistry;

public class ExceptionUtils_meterRuntimeException_5_0_Test_meterRuntimeException_marksMeter_once {

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
        // Attempt to remove final modifier if possible; on some JVMs the 'modifiers' field does not exist.
        try {
            modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            try {
                modifiersField.setInt(runtimeExceptionMeterField, runtimeExceptionMeterField.getModifiers() & ~Modifier.FINAL);
            } catch (IllegalAccessException ignored) {
                // If we cannot change modifiers, proceed; setting the field may still work.
            }
        } catch (NoSuchFieldException ignored) {
            // Some JVMs (modules) do not expose 'modifiers' field; ignore.
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        // Restore original value (most important). Ignore failures on some JVMs.
        try {
            runtimeExceptionMeterField.set(null, originalRuntimeExceptionMeter);
        } catch (IllegalAccessException | IllegalArgumentException ignored) {
            // In some JVMs updating a static final field via reflection may not be allowed;
            // ignore since test modifications are limited to the single test execution.
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
    void meterRuntimeException_marksMeter_once() throws Exception {
        CountingMeter cm = new CountingMeter();
        runtimeExceptionMeterField.set(null, cm);
        ExceptionUtils.meterRuntimeException();
        assertEquals(1, cm.getCount(), "meterRuntimeException should mark the meter once");
    }


}
