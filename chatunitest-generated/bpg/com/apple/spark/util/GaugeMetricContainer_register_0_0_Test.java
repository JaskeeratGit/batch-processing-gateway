package com.apple.spark.util;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import io.micrometer.core.instrument.Gauge;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Unit tests for GaugeMetricContainer.register(String, Supplier, Tag...).
 *
 * These tests verify that the varargs overload delegates to the collection-based overload,
 * preserving the metric name, supplier instance, and the order/content of the Tag list.
 *
 * The tests use a small test subclass that "captures" the invocation of the collection-based
 * register method. Reflection is used to invoke the collection overload reflectively.
 */
public class GaugeMetricContainer_register_0_0_Test {

    private MeterRegistry meterRegistry;

    @BeforeEach
    public void setUp() {
        // Use a real lightweight registry to avoid Mockito initialization issues.
        meterRegistry = new SimpleMeterRegistry();
    }

    /**
     * A test subclass that captures invocations to the collection-based register method.
     *
     * Declares a method with signature (String, Supplier<Number>, Collection<Tag>). If the real
     * GaugeMetricContainer defines that method as protected/public, this will override it.
     * If the real class defines it as private, this will be a separate method; tests still
     * exercise behavior by invoking the public varargs method (which will call the class'
     * collection overload) and by reflective invocation of the collection overload on this
     * instance.
     */
    static class TestGaugeMetricContainer extends GaugeMetricContainer {

        final AtomicReference<Captured> capturedRef = new AtomicReference<>();

        public TestGaugeMetricContainer(MeterRegistry meterRegistry) {
            super(meterRegistry);
        }

        // Intentionally declare this method (no @Override) to either override or shadow the base method.
        // Use a null-tolerant copy to preserve possible null elements in the tags list.
        public void register(String metricName, Supplier<Number> valueProvider, Collection<Tag> tags) {
            List<Tag> copied = tags == null ? null : tags.stream().collect(Collectors.toList());
            capturedRef.set(new Captured(metricName, valueProvider, copied));
        }
    }

    static final class Captured {

        final String metricName;

        final Supplier<Number> supplier;

        final List<Tag> tags;

        Captured(String metricName, Supplier<Number> supplier, List<Tag> tags) {
            this.metricName = metricName;
            this.supplier = supplier;
            this.tags = tags;
        }
    }

    @Test
    public void testVarargsDelegatesToCollectionAndPreservesOrder() {
        TestGaugeMetricContainer container = new TestGaugeMetricContainer(meterRegistry);
        Supplier<Number> supplier = () -> 123;
        Tag t1 = Tag.of("k1", "v1");
        Tag t2 = Tag.of("k2", "v2");
        // Call the varargs overload
        container.register("metric.name", supplier, t1, t2);
        Captured c = container.capturedRef.get();
        assertNotNull(c, "Expected the collection overload to be invoked and captured");
        assertEquals("metric.name", c.metricName);
        assertSame(supplier, c.supplier);
        assertNotNull(c.tags);
        assertEquals(2, c.tags.size());
        assertSame(t1, c.tags.get(0));
        assertSame(t2, c.tags.get(1));
    }

    @Test
    public void testVarargsWithNoTagsProducesEmptyList() {
        TestGaugeMetricContainer container = new TestGaugeMetricContainer(meterRegistry);
        Supplier<Number> supplier = () -> 7;
        // Call the varargs overload with no tags
        container.register("no.tags.metric", supplier);
        Captured c = container.capturedRef.get();
        assertNotNull(c, "Expected the collection overload to be invoked and captured");
        assertEquals("no.tags.metric", c.metricName);
        assertSame(supplier, c.supplier);
        assertNotNull(c.tags, "Empty varargs should produce an empty list, not null");
        assertEquals(0, c.tags.size());
    }

    @Test
    public void testVarargsWithNullElementIsPreservedInList() {
        TestGaugeMetricContainer container = new TestGaugeMetricContainer(meterRegistry);
        Supplier<Number> supplier = () -> 9;
        Tag nullTag = null;
        // Call the varargs overload with a single null tag element
        container.register("null.element.metric", supplier, nullTag);
        Captured c = container.capturedRef.get();
        assertNotNull(c, "Expected the collection overload to be invoked and captured");
        assertEquals("null.element.metric", c.metricName);
        assertSame(supplier, c.supplier);
        assertNotNull(c.tags, "Tags list should not be null when varargs provided");
        assertEquals(1, c.tags.size());
        assertNull(c.tags.get(0), "Null element in varargs should be preserved in the list");
    }

    @Test
    public void testDirectInvocationOfCollectionOverloadViaReflection() throws Exception {
        TestGaugeMetricContainer container = new TestGaugeMetricContainer(meterRegistry);
        Supplier<Number> supplier = () -> 5;
        Tag t = Tag.of("x", "y");
        List<Tag> tagList = Arrays.asList(t);
        // Reflectively invoke the collection overload (String, Supplier, Collection)
        Method method = container.getClass().getMethod("register", String.class, Supplier.class, Collection.class);
        method.invoke(container, "reflect.metric", supplier, tagList);
        Captured c = container.capturedRef.get();
        assertNotNull(c, "Expected the reflective invocation to be captured");
        assertEquals("reflect.metric", c.metricName);
        assertSame(supplier, c.supplier);
        assertNotNull(c.tags);
        assertEquals(1, c.tags.size());
        assertSame(t, c.tags.get(0));
    }
}
