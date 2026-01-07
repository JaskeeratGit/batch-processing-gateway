package com.apple.spark.util;

import io.micrometer.core.instrument.Tag;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TimerMetricContainer_record_2_0_Test_forwardsToCollectionRecord_withNonEmptyTags {

    // A test subclass that overrides the presumed record(Supplier, String, Collection<Tag>) method
    // so we can verify that the varargs-forwarding focal method delegates correctly.
    static class TestableTimerMetricContainer extends TimerMetricContainer {

        boolean called = false;

        Supplier<?> capturedSupplier;

        String capturedMetricName;

        Collection<Tag> capturedTags;

        public TestableTimerMetricContainer() {
            // TimerMetricContainer requires a MeterRegistry in constructor. The focal method
            // used in these tests does not use it, so passing null is acceptable for unit testing.
            super(null);
        }

        @Override
        public <T> T record(Supplier<T> runnable, String metricName, Collection<Tag> tags) {
            this.called = true;
            this.capturedSupplier = runnable;
            this.capturedMetricName = metricName;
            this.capturedTags = tags;
            return runnable.get();
        }
    }

    // Simple Tag implementation for tests
    private static Tag tag(String k, String v) {
        return new Tag() {

            @Override
            public String getKey() {
                return k;
            }

            @Override
            public String getValue() {
                return v;
            }

            @Override
            public String toString() {
                return k + "=" + v;
            }
        };
    }

    @Test
    public void forwardsToCollectionRecord_withNonEmptyTags() throws Exception {
        TestableTimerMetricContainer container = new TestableTimerMetricContainer();
        Tag t1 = tag("k1", "v1");
        Tag t2 = tag("k2", "v2");
        String result = container.record(() -> "ok", "metric.name", t1, t2);
        assertEquals("ok", result);
        assertTrue(container.called, "The collection-based record should have been called");
        assertEquals("metric.name", container.capturedMetricName);
        assertNotNull(container.capturedTags);
        assertEquals(2, container.capturedTags.size());
        // identity contains works because we used the same instances
        assertTrue(container.capturedTags.contains(t1));
        assertTrue(container.capturedTags.contains(t2));
        // reflectively inspect the private timers field to improve coverage of internal state access
        Field timersField = TimerMetricContainer.class.getDeclaredField("timers");
        timersField.setAccessible(true);
        Object timersObj = timersField.get(container);
        assertNotNull(timersObj);
        assertTrue(timersObj instanceof ConcurrentHashMap);
        assertEquals(0, ((ConcurrentHashMap<?, ?>) timersObj).size());
    }




}
