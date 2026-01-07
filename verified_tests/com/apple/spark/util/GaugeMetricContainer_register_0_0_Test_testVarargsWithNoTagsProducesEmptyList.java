package com.apple.spark.util;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.MeterRegistry;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import io.micrometer.core.instrument.Gauge;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Unit tests for GaugeMetricContainer.register(String, Supplier, Tag...).
 *
 * These tests verify that the varargs overload delegates to the collection-based overload,
 * preserving the metric name, supplier instance, and the order/content of the Tag list.
 *
 * The tests use a small test subclass that "captures" the invocation of the collection-based
 * register method. Reflection is used to invoke the public varargs method and also to invoke
 * the collection overload reflectively (to handle the case it might be private).
 */
public class GaugeMetricContainer_register_0_0_Test_testVarargsWithNoTagsProducesEmptyList {

    private MeterRegistry meterRegistry;

    @BeforeEach
    public void setUp() {
        meterRegistry = Mockito.mock(MeterRegistry.class);
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
        public void register(String metricName, Supplier<Number> valueProvider, Collection<Tag> tags) {
            capturedRef.set(new Captured(metricName, valueProvider, tags == null ? null : List.copyOf(tags)));
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
    public void testVarargsWithNoTagsProducesEmptyList() throws Exception {
        TestGaugeMetricContainer container = new TestGaugeMetricContainer(meterRegistry);
        Supplier<Number> supplier = () -> 7;
        Method varargsMethod = GaugeMetricContainer.class.getMethod("register", String.class, Supplier.class, Tag[].class);
        // Invoke with an empty array (equivalent to calling register("name", supplier) with no tags)
        varargsMethod.invoke(container, "empty.metric", supplier, new Tag[] {});
        Captured captured = container.capturedRef.get();
        assertNotNull(captured, "Expected the collection-based register to be invoked and captured");
        assertEquals("empty.metric", captured.metricName);
        assertSame(supplier, captured.supplier);
        assertNotNull(captured.tags);
        assertEquals(0, captured.tags.size());
    }


}
