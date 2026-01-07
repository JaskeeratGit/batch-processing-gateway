package com.apple.spark.util;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

class TimerMetricContainer_record_0_0_Test_testRecordVarargsDelegatesWhenCalledReflectively {

    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        meterRegistry = mock(MeterRegistry.class);
    }

    /**
     * A test subclass that overrides the collection-based record method to capture incoming parameters.
     * This allows verifying that the varargs record(...) delegates correctly to the collection overload.
     *
     * Note: The overridden method uses Collection<Tag> to match the likely signature of the other overload.
     * If the original class uses a more specific type (e.g., List<Tag>), this subclass will still work in
     * most common implementations where Collection is the declared type. This test lives in the same
     * package as the class under test.
     */
    static class TestTimerMetricContainer extends TimerMetricContainer {

        boolean invoked = false;

        Runnable capturedRunnable = null;

        String capturedMetricName = null;

        Collection<Tag> capturedTags = null;

        TestTimerMetricContainer(MeterRegistry registry) {
            super(registry);
        }

        // Override the collection-based overload to capture invocation.
        @Override
        public void record(Runnable runnable, String metricName, Collection<Tag> tags) {
            this.invoked = true;
            this.capturedRunnable = runnable;
            this.capturedMetricName = metricName;
            this.capturedTags = tags;
            // Do not execute the runnable here to allow asserting whether runnable was run by caller or not.
        }
    }


    @Test
    void testRecordVarargsDelegatesWhenCalledReflectively() throws Exception {
        TestTimerMetricContainer container = new TestTimerMetricContainer(meterRegistry);
        AtomicInteger runCount = new AtomicInteger(0);
        Runnable runnable = runCount::incrementAndGet;
        Tag t1 = mock(Tag.class);
        Tag[] tags = new Tag[] { t1 };
        // Use reflection to invoke the varargs method (also covers reflective invocation path)
        Method varargsMethod = TimerMetricContainer.class.getMethod("record", Runnable.class, String.class, Tag[].class);
        // defensive; method is public but we set accessible anyway
        varargsMethod.setAccessible(true);
        varargsMethod.invoke(container, runnable, "reflect.metric", tags);
        assertTrue(container.invoked, "Expected reflective call to varargs method to delegate to collection overload");
        assertSame(runnable, container.capturedRunnable);
        assertEquals("reflect.metric", container.capturedMetricName);
        assertEquals(Arrays.asList(tags), List.copyOf(container.capturedTags));
        assertEquals(0, runCount.get(), "Runnable should not have been executed by the overridden collection overload");
    }


}
