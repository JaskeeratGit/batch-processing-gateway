package com.apple.spark.util;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/**
 * JUnit 5 tests for TimerMetricContainer#record(Supplier, String, Collection)
 *
 * Uses Mockito to mock MeterRegistry and Timer.
 * Uses reflection to populate the internal timers map so the production code
 * will return the mocked Timer without invoking MeterRegistry default methods.
 */
@ExtendWith(MockitoExtension.class)
class TimerMetricContainer_record_3_0_Test_record_shouldDelegateToTimerAndReturnValue {

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Timer timer;

    private TimerMetricContainer container;

    @BeforeEach
    void setUp() {
        container = new TimerMetricContainer(meterRegistry);
    }

    @Test
    void record_shouldDelegateToTimerAndReturnValue() throws Exception {
        String metricName = "test.metric";
        Tag t1 = Tag.of("k1", "v1");
        Tag t2 = Tag.of("k2", "v2");
        Collection<Tag> tags = Arrays.asList(t1, t2);

        // Populate the internal timers map so getTimer returns our mocked timer
        Field timersField = TimerMetricContainer.class.getDeclaredField("timers");
        timersField.setAccessible(true);
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<Object, Timer> timersMap = (ConcurrentHashMap<Object, Timer>) timersField.get(container);

        // Find the MetricId inner class and construct an instance for the given metricName and tags
        Class<?> metricIdClass = Arrays.stream(TimerMetricContainer.class.getDeclaredClasses())
                .filter(c -> c.getSimpleName().equals("MetricId"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("MetricId inner class not found"));
        Constructor<?> ctor = metricIdClass.getDeclaredConstructor(String.class, Collection.class);
        ctor.setAccessible(true);
        Object metricId = ctor.newInstance(metricName, tags);

        timersMap.put(metricId, timer);

        // Make timer.record call the supplied Supplier so we exercise supplier logic and return its value.
        doAnswer(invocation -> {
            Supplier<?> supplied = (Supplier<?>) invocation.getArgument(0);
            return supplied.get();
        }).when(timer).record(ArgumentMatchers.any(Supplier.class));

        Supplier<String> supplier = () -> "ok-value";
        String result = container.record(supplier, metricName, tags);
        assertEquals("ok-value", result);

        verify(timer).record(supplier);
    }



}
