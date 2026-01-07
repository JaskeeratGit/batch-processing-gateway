package com.apple.spark.util;

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
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

class TimerMetricContainer_record_0_0_Test {

    /**
     * A test subclass that captures calls to the collection-based overload.
     * Note: no @Override annotation to remain compatible whether the superclass
     * declares the Collection overload or not; if it does, this will still override it.
     */
    static class TestTimerMetricContainer extends TimerMetricContainer {

        boolean invoked = false;

        Runnable capturedRunnable = null;

        String capturedMetricName = null;

        Collection<Tag> capturedTags = null;

        TestTimerMetricContainer() {
            // passing null MeterRegistry because tests don't use it
            super(null);
        }

        public void record(Runnable runnable, String metricName, Collection<Tag> tags) {
            this.invoked = true;
            this.capturedRunnable = runnable;
            this.capturedMetricName = metricName;
            this.capturedTags = tags;
            // Do not run the runnable here so tests can assert behavior separately.
        }
    }

    @Test
    void testRecordVarargsDelegatesToCollectionOverload() {
        TestTimerMetricContainer container = new TestTimerMetricContainer();
        AtomicInteger runCount = new AtomicInteger(0);
        Runnable runnable = runCount::incrementAndGet;
        Tag t1 = Tag.of("k1", "v1");
        Tag t2 = Tag.of("k2", "v2");
        container.record(runnable, "my.metric", t1, t2);
        assertTrue(container.invoked, "Expected collection-based overload to be invoked");
        assertSame(runnable, container.capturedRunnable, "Runnable should be passed through unchanged");
        assertEquals("my.metric", container.capturedMetricName);
        assertNotNull(container.capturedTags);
        List<Tag> capturedList = Arrays.asList(container.capturedTags.toArray(new Tag[0]));
        // verify both tags present with correct key/value
        assertEquals(2, capturedList.size());
        assertTrue(capturedList.stream().anyMatch(tag -> "k1".equals(tag.getKey()) && "v1".equals(tag.getValue())));
        assertTrue(capturedList.stream().anyMatch(tag -> "k2".equals(tag.getKey()) && "v2".equals(tag.getValue())));
        // ensure runnable was not executed by the override
        assertEquals(0, runCount.get());
    }

    @Test
    void testRecordVarargsDelegatesWhenCalledReflectively() throws Exception {
        TestTimerMetricContainer container = new TestTimerMetricContainer();
        Runnable runnable = () -> {
        };
        Tag t1 = Tag.of("rk", "rv");
        // Reflectively invoke the varargs method: signature (Runnable, String, Tag...)
        Method m = TimerMetricContainer.class.getMethod("record", Runnable.class, String.class, Tag[].class);
        // When using Method.invoke for a varargs parameter, pass the array as the last element in the Object[].
        m.invoke(container, new Object[] { runnable, "reflect.metric", new Tag[] { t1 } });
        assertTrue(container.invoked, "Expected collection-based overload to be invoked via reflective varargs call");
        assertSame(runnable, container.capturedRunnable);
        assertEquals("reflect.metric", container.capturedMetricName);
        assertNotNull(container.capturedTags);
        assertEquals(1, container.capturedTags.size());
        Tag captured = container.capturedTags.iterator().next();
        assertEquals("rk", captured.getKey());
        assertEquals("rv", captured.getValue());
    }

    @Test
    void testRecordVarargsWithNullTagsThrowsNullPointerException() {
        TimerMetricContainer container = new TimerMetricContainer(null);
        assertThrows(NullPointerException.class, () -> container.record(() -> {
        }, "null.tags.metric", (Tag[]) null));
    }

    @Test
    void testRecordVarargsWithEmptyTagsDelegatesWithEmptyList() {
        TestTimerMetricContainer container = new TestTimerMetricContainer();
        Runnable runnable = () -> {
        };
        // call with no tags (empty varargs)
        container.record(runnable, "empty.tags.metric");
        assertTrue(container.invoked);
        assertSame(runnable, container.capturedRunnable);
        assertEquals("empty.tags.metric", container.capturedMetricName);
        assertNotNull(container.capturedTags);
        assertTrue(container.capturedTags.isEmpty(), "Expected an empty collection when called with no tags");
    }
}
