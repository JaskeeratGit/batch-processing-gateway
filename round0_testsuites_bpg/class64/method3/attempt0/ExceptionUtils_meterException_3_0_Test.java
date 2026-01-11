package com.apple.spark.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicLong;
import com.codahale.metrics.Meter;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.apple.spark.core.Constants;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;

/**
 * JUnit 5 tests for ExceptionUtils.meterException().
 */
public class ExceptionUtils_meterException_3_0_Test {

    private Object originalExceptionMeter;

    @BeforeEach
    public void setUp() throws Exception {
        Field f = ExceptionUtils.class.getDeclaredField("exceptionMeter");
        f.setAccessible(true);
        originalExceptionMeter = f.get(null);
    }

    @AfterEach
    public void tearDown() throws Exception {
        // Restore original static final field to avoid side effects for other tests
        Field f = ExceptionUtils.class.getDeclaredField("exceptionMeter");
        setFinalStatic(f, originalExceptionMeter);
    }

    @Test
    public void testMeterException_marksOnce() throws Exception {
        TestMeter testMeter = new TestMeter();
        Field f = ExceptionUtils.class.getDeclaredField("exceptionMeter");
        setFinalStatic(f, testMeter);
        // Call the focal method
        ExceptionUtils.meterException();
        // Verify mark was called exactly once
        assertEquals(1L, testMeter.getMarkCount());
    }

    @Test
    public void testMeterException_multipleCalls_accumulatesMarks() throws Exception {
        TestMeter testMeter = new TestMeter();
        Field f = ExceptionUtils.class.getDeclaredField("exceptionMeter");
        setFinalStatic(f, testMeter);
        // Call multiple times
        ExceptionUtils.meterException();
        ExceptionUtils.meterException();
        ExceptionUtils.meterException();
        // Verify mark was called three times
        assertEquals(3L, testMeter.getMarkCount());
    }

    @Test
    public void testMeterException_replaceMeter_midTest_behavesCorrectly() throws Exception {
        TestMeter first = new TestMeter();
        TestMeter second = new TestMeter();
        Field f = ExceptionUtils.class.getDeclaredField("exceptionMeter");
        setFinalStatic(f, first);
        ExceptionUtils.meterException();
        assertEquals(1L, first.getMarkCount());
        assertEquals(0L, second.getMarkCount());
        // Replace the static final meter with a different instance and ensure subsequent marks go to the new meter
        setFinalStatic(f, second);
        ExceptionUtils.meterException();
        assertEquals(1L, first.getMarkCount(), "first meter should not increase after replacement");
        assertEquals(1L, second.getMarkCount(), "second meter should get marks after replacement");
    }

    // Helper to set private static final fields via reflection
    private static void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);
        // Remove final modifier from the field
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        // Set the new value
        field.set(null, newValue);
    }

    /**
     * Simple Meter subclass to capture mark() invocations.
     */
    private static class TestMeter extends Meter {

        private final AtomicLong count = new AtomicLong(0);

        @Override
        public void mark() {
            count.incrementAndGet();
            super.mark();
        }

        @Override
        public void mark(long n) {
            count.addAndGet(n);
            super.mark(n);
        }

        long getMarkCount() {
            return count.get();
        }
    }
}
