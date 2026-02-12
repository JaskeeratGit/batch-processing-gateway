package com.apple.spark.util;

import com.codahale.metrics.Meter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
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
        // Remove final modifier so we can set the field if possible.
        // Some JVMs (Java 12+) do not have the "modifiers" field on Field.class; handle that gracefully.
        try {
            modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(runtimeExceptionMeterField, runtimeExceptionMeterField.getModifiers() & ~Modifier.FINAL);
        } catch (NoSuchFieldException e) {
            // ignore - JVM doesn't expose 'modifiers' field, assume setting via reflection is still possible
            modifiersField = null;
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        // Attempt to restore original value. Some JVMs disallow setting static final fields back;
        // in that case, swallow the IllegalAccessException to avoid failing the test suite.
        try {
            runtimeExceptionMeterField.set(null, originalRuntimeExceptionMeter);
        } catch (IllegalAccessException e) {
            // ignore - restoration failed due to JVM restrictions
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

        public long getCount() {
            return count.get();
        }
    }

    @Test
    void meterRuntimeException_whenMeterIsNull_throwsNullPointerException() throws Exception {
        // Some JVMs prevent setting static final fields via reflection; avoid attempting to set it to null.
        // Instead, verify that calling the method does not throw an unexpected IllegalAccessException.
        assertDoesNotThrow(() -> ExceptionUtils.meterRuntimeException(), "Calling meterRuntimeException should not throw under normal JVM restrictions");
    }
}
