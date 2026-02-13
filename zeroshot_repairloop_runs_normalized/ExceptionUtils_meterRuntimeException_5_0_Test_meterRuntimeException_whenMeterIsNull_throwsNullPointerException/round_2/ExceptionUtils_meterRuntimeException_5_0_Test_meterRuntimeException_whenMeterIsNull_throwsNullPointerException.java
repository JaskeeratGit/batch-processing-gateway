package com.apple.spark.util;

import com.codahale.metrics.Meter;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;

public class ExceptionUtils_meterRuntimeException_5_0_Test_meterRuntimeException_whenMeterIsNull_throwsNullPointerException {

    private Field runtimeExceptionMeterField;

    private Object originalRuntimeExceptionMeter;

    @BeforeEach
    void setUp() throws Exception {
        // Access the private static final field
        runtimeExceptionMeterField = ExceptionUtils.class.getDeclaredField("runtimeExceptionMeter");
        runtimeExceptionMeterField.setAccessible(true);
        // Keep original to restore later
        originalRuntimeExceptionMeter = runtimeExceptionMeterField.get(null);
        // Do not attempt to modify 'final' modifiers via reflection (not available on some JVMs)
        // Rely on setting accessible to allow updating the reference for testing.
    }

    @AfterEach
    void tearDown() throws Exception {
        // Restore original value (restore value is most important)
        try {
            runtimeExceptionMeterField.set(null, originalRuntimeExceptionMeter);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            // Some JVMs do not allow resetting of static final fields via reflection; ignore if we cannot restore.
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
        // Set the metric field to null to exercise behavior when field is null
        runtimeExceptionMeterField.set(null, null);
        assertThrows(NullPointerException.class, () -> ExceptionUtils.meterRuntimeException(), "Calling meterRuntimeException when the meter is null should throw NPE");
    }
}
