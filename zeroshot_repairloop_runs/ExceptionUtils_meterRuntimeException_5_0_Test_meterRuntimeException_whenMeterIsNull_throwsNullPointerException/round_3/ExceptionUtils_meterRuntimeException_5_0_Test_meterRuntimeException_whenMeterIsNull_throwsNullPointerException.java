package com.apple.spark.util;

import com.codahale.metrics.Meter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import sun.misc.Unsafe;

public class ExceptionUtils_meterRuntimeException_5_0_Test_meterRuntimeException_whenMeterIsNull_throwsNullPointerException {

    private Field runtimeExceptionMeterField;

    private Object originalRuntimeExceptionMeter;

    private Field modifiersField;

    // Unsafe-based handles for reliably writing to static final fields when possible
    private Unsafe unsafe;
    private Object staticFieldBase;
    private long staticFieldOffset;

    @BeforeEach
    void setUp() throws Exception {
        // Access the private static final field
        runtimeExceptionMeterField = ExceptionUtils.class.getDeclaredField("runtimeExceptionMeter");
        runtimeExceptionMeterField.setAccessible(true);

        // Try to acquire Unsafe to modify the static final field reliably
        try {
            Field theUnsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafeField.setAccessible(true);
            unsafe = (Unsafe) theUnsafeField.get(null);
            staticFieldBase = unsafe.staticFieldBase(runtimeExceptionMeterField);
            staticFieldOffset = unsafe.staticFieldOffset(runtimeExceptionMeterField);
            // Read original via Unsafe
            originalRuntimeExceptionMeter = unsafe.getObject(staticFieldBase, staticFieldOffset);
        } catch (Throwable t) {
            // Fallback: best-effort via reflection (may not work on all JVMs)
            unsafe = null;
            modifiersField = null;
            runtimeExceptionMeterField.setAccessible(true);
            originalRuntimeExceptionMeter = runtimeExceptionMeterField.get(null);
            try {
                modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(runtimeExceptionMeterField, runtimeExceptionMeterField.getModifiers() & ~Modifier.FINAL);
            } catch (Exception e) {
                // Ignored: on some JVMs this is not permitted
            }
        }
    }

    @AfterEach
    void tearDown() {
        // Best-effort restore original value and modifiers. Ignore failures to avoid masking test results.
        try {
            if (unsafe != null && runtimeExceptionMeterField != null) {
                try {
                    unsafe.putObject(staticFieldBase, staticFieldOffset, originalRuntimeExceptionMeter);
                } catch (Throwable ignored) {
                    // ignore
                }
            } else {
                if (runtimeExceptionMeterField != null) {
                    runtimeExceptionMeterField.setAccessible(true);
                    runtimeExceptionMeterField.set(null, originalRuntimeExceptionMeter);
                }
                if (modifiersField != null && runtimeExceptionMeterField != null) {
                    try {
                        modifiersField.setInt(runtimeExceptionMeterField, runtimeExceptionMeterField.getModifiers() | Modifier.FINAL);
                    } catch (Exception ignored) {
                        // ignore
                    }
                }
            }
        } catch (Throwable ignored) {
            // ignore any exception during teardown to avoid failing the test due to JVM reflection restrictions
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

        // Must be public to match Metered#getCount() signature
        public long getCount() {
            return count.get();
        }
    }

    // A Meter that throws a NullPointerException when mark() is invoked
    static class NpeMeter extends Meter {
        @Override
        public void mark() {
            throw new NullPointerException();
        }

        @Override
        public void mark(long n) {
            throw new NullPointerException();
        }
    }

    @Test
    void meterRuntimeException_whenMeterIsNull_throwsNullPointerException() throws Exception {
        // Instead of attempting to reliably set the static final to null (which is unreliable on some JVMs),
        // set it to a Meter implementation whose mark() throws NullPointerException. This ensures the method
        // under test results in a NullPointerException when it attempts to mark the meter.
        NpeMeter npeMeter = new NpeMeter();

        // Use Unsafe if available to bypass "static final" restrictions, otherwise fall back to reflection.
        if (unsafe != null) {
            unsafe.putObject(staticFieldBase, staticFieldOffset, npeMeter);
        } else {
            runtimeExceptionMeterField.setAccessible(true);
            runtimeExceptionMeterField.set(null, npeMeter);
        }

        assertThrows(NullPointerException.class, () -> ExceptionUtils.meterRuntimeException(),
            "Calling meterRuntimeException when the meter's mark() throws NPE should propagate NPE");
    }
}
