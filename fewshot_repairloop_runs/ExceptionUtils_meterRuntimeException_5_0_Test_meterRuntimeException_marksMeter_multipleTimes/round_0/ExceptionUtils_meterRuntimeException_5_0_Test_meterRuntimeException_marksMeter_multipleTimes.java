package com.apple.spark.util;

import com.codahale.metrics.Meter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import com.apple.spark.core.Constants;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;

public class ExceptionUtils_meterRuntimeException_5_0_Test_meterRuntimeException_marksMeter_multipleTimes {

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
        // Restore original value and modifiers (restore value is most important)
        runtimeExceptionMeterField.set(null, originalRuntimeExceptionMeter);
    }

    // A simple Meter subclass that counts mark invocations
    static class CountingMeter extends Meter {

        private final AtomicLong count = new AtomicLong(0L);

        @Override
        public void mark() {
            count.incrementAndGet();
        }

        @Override
        public void mark(long n) {
            count.addAndGet(n);
        }

        public long getCount() {
            return count.get();
        }
    }


    @Test
    void meterRuntimeException_marksMeter_multipleTimes() throws Exception {
        CountingMeter cm = new CountingMeter();
        runtimeExceptionMeterField.set(null, cm);
        ExceptionUtils.meterRuntimeException();
        ExceptionUtils.meterRuntimeException();
        assertEquals(2L, cm.getCount(), "meterRuntimeException called twice should increment meter twice");
    }

}
