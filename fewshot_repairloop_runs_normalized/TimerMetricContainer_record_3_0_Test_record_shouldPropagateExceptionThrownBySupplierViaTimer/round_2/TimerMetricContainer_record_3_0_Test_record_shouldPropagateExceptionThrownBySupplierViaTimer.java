package com.apple.spark.util;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JUnit 5 tests for TimerMetricContainer#record(Supplier, String, Collection)
 *
 * Uses Mockito to mock MeterRegistry and Timer.
 * Uses reflection to invoke the private getTimer(String, Collection<Tag>) method.
 */
@ExtendWith(MockitoExtension.class)
class TimerMetricContainer_record_3_0_Test_record_shouldPropagateExceptionThrownBySupplierViaTimer {

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
    void record_shouldPropagateExceptionThrownBySupplierViaTimer() throws Exception {
        String metricName = "error.metric";
        Collection<Tag> tags = Collections.emptyList();

        // Pre-populate the private timers map to return our mocked timer instead of triggering Timer.builder.register(...)
        Field timersField = TimerMetricContainer.class.getDeclaredField("timers");
        timersField.setAccessible(true);
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<Object, Timer> timersMap = (ConcurrentHashMap<Object, Timer>) timersField.get(container);
        if (timersMap == null) {
            timersMap = new ConcurrentHashMap<>();
            timersField.set(container, timersMap);
        }

        // Find the inner class that acts as the metric id and construct an instance matching how TimerMetricContainer builds the key
        Class<?> metricIdClass = null;
        Constructor<?> chosenCtor = null;
        for (Class<?> c : TimerMetricContainer.class.getDeclaredClasses()) {
            for (Constructor<?> ctor : c.getDeclaredConstructors()) {
                Class<?>[] pts = ctor.getParameterTypes();
                if (pts.length == 2 && pts[0] == String.class && Collection.class.isAssignableFrom(pts[1])) {
                    metricIdClass = c;
                    chosenCtor = ctor;
                    break;
                }
                if (pts.length == 3 && pts[0] == TimerMetricContainer.class && pts[1] == String.class
                        && Collection.class.isAssignableFrom(pts[2])) {
                    metricIdClass = c;
                    chosenCtor = ctor;
                    break;
                }
            }
            if (chosenCtor != null) {
                break;
            }
        }

        if (chosenCtor == null) {
            throw new IllegalStateException("Suitable MetricId constructor not found");
        }
        chosenCtor.setAccessible(true);

        Object metricId;
        if (chosenCtor.getParameterCount() == 2) {
            // static nested: (String, Collection<Tag>)
            metricId = chosenCtor.newInstance(metricName, tags);
        } else {
            // non-static inner: (TimerMetricContainer, String, Collection<Tag>)
            metricId = chosenCtor.newInstance(container, metricName, tags);
        }

        timersMap.put(metricId, timer);

        when(meterRegistry.timer(eq(metricName), eq(tags))).thenReturn(timer);
        // timer.record will invoke the supplier and therefore throw
        when(timer.record(ArgumentMatchers.<Supplier<?>>any())).thenAnswer(invocation -> {
            Supplier<?> s = invocation.getArgument(0);
            // will throw
            return s.get();
        });
        Supplier<String> throwingSupplier = () -> {
            throw new IllegalStateException("boom");
        };
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> container.record(throwingSupplier, metricName, tags));
        assertEquals("boom", ex.getMessage());
        verify(meterRegistry).timer(metricName, tags);
        verify(timer).record(throwingSupplier);
    }


}
