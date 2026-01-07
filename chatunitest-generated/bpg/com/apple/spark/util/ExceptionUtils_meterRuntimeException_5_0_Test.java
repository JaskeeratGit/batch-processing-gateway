package com.apple.spark.util;

import com.codahale.metrics.Meter;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
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

public class ExceptionUtils_meterRuntimeException_5_0_Test {

    private Field runtimeExceptionMeterField;

    private Object originalRuntimeExceptionMeter;

    @BeforeEach
    void setUp() throws Exception {
        // Access the private static final field
        runtimeExceptionMeterField = ExceptionUtils.class.getDeclaredField("runtimeExceptionMeter");
        // Save original value to restore later
        runtimeExceptionMeterField.setAccessible(true);
        originalRuntimeExceptionMeter = runtimeExceptionMeterField.get(null);
    }

    @AfterEach
    void tearDown() throws Exception {
        // Restore original value (most important)
        setStaticField(runtimeExceptionMeterField, originalRuntimeExceptionMeter);
    }

    // Helper that tries multiple strategies to set a private static final field
    private static void setStaticField(Field field, Object value) throws Exception {
        field.setAccessible(true);
        try {
            // Try simple reflection set first
            field.set(null, value);
            return;
        } catch (IllegalAccessException | IllegalArgumentException ignored) {
            // fallthrough to other strategies
        }
        // Try VarHandle (Java 9+)
        try {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(field.getDeclaringClass(), MethodHandles.lookup());
            VarHandle vh = lookup.findStaticVarHandle(field.getDeclaringClass(), field.getName(), field.getType());
            vh.set(value);
            return;
        } catch (Throwable ignored) {
            // fallthrough to Unsafe strategy
        }
        // Last resort: use sun.misc.Unsafe
        try {
            Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);
            Object base = unsafe.staticFieldBase(field);
            long offset = unsafe.staticFieldOffset(field);
            unsafe.putObject(base, offset, value);
            return;
        } catch (Throwable t) {
            throw new RuntimeException("Unable to set static field " + field, t);
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
        setStaticField(runtimeExceptionMeterField, cm);
        // Call the method under test
        ExceptionUtils.meterRuntimeException();
        assertEquals(1L, cm.getCount(), "meter should have been marked once");
    }

    @Test
    void meterRuntimeException_marksMeter_multipleTimes() throws Exception {
        CountingMeter cm = new CountingMeter();
        setStaticField(runtimeExceptionMeterField, cm);
        // Call the method multiple times
        ExceptionUtils.meterRuntimeException();
        ExceptionUtils.meterRuntimeException();
        ExceptionUtils.meterRuntimeException();
        assertEquals(3L, cm.getCount(), "meter should have been marked three times");
    }

    @Test
    void meterRuntimeException_whenMeterIsNull_throwsNullPointerException() throws Exception {
        // Set the meter to null and expect NPE when invoking
        setStaticField(runtimeExceptionMeterField, null);
        assertThrows(NullPointerException.class, () -> ExceptionUtils.meterRuntimeException());
    }
}
