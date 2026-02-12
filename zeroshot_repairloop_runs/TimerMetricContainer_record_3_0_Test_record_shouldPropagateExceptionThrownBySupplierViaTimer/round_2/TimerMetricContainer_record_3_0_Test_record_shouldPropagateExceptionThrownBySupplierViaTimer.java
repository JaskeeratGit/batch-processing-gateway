package com.apple.spark.util;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;
import org.mockito.*;
import org.junit.jupiter.api.*;
import java.lang.reflect.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for TimerMetricContainer#record(Supplier, String, Collection)
 *
 * Uses Mockito to mock MeterRegistry and Timer.
 * Uses reflection to pre-populate the private timers map so the Timer builder
 * (which would try to use meterRegistry.config()) is not invoked during the test.
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

        // Pre-populate the private timers map in TimerMetricContainer to avoid executing Timer.builder(...).register(meterRegistry)
        // which would try to access meterRegistry.config() and cause a NullPointerException on the mocked MeterRegistry.
        Class<?> containerClass = TimerMetricContainer.class;

        // Try to find and instantiate the MetricId-like nested class by probing nested classes and their constructors.
        Object metricIdKey = null;
        Class<?> metricIdClass = null;
        for (Class<?> nested : containerClass.getDeclaredClasses()) {
            Constructor<?>[] ctors = nested.getDeclaredConstructors();
            for (Constructor<?> ctor : ctors) {
                ctor.setAccessible(true);
                Class<?>[] paramTypes = ctor.getParameterTypes();
                // Look for constructors whose first parameter is String (metric name)
                if (paramTypes.length >= 1 && String.class.isAssignableFrom(paramTypes[0])) {
                    Object[] args = new Object[paramTypes.length];
                    args[0] = metricName;
                    if (paramTypes.length >= 2) {
                        Class<?> second = paramTypes[1];
                        if (Collection.class.isAssignableFrom(second)) {
                            args[1] = tags;
                        } else if (second.isArray() && Tag.class.isAssignableFrom(second.getComponentType())) {
                            args[1] = tags.toArray(new Tag[0]);
                        } else if (java.util.List.class.isAssignableFrom(second)) {
                            args[1] = new ArrayList<>(tags);
                        } else {
                            args[1] = tags;
                        }
                    }
                    // fill remaining args with nulls
                    for (int i = 2; i < paramTypes.length; i++) {
                        args[i] = null;
                    }
                    try {
                        metricIdKey = ctor.newInstance(args);
                        metricIdClass = nested;
                        break;
                    } catch (Exception e) {
                        // try next constructor
                    }
                }
            }
            if (metricIdKey != null) break;
        }

        if (metricIdKey == null) {
            throw new IllegalStateException("Unable to construct MetricId-like nested class instance via reflection");
        }

        // Put the mock timer into the private timers map
        Field timersField = containerClass.getDeclaredField("timers");
        timersField.setAccessible(true);
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<Object, Timer> timersMap = (ConcurrentHashMap<Object, Timer>) timersField.get(container);
        timersMap.put(metricIdKey, timer);

        // timer.record will invoke the supplier and therefore throw
        when(timer.record(ArgumentMatchers.<Supplier<?>>any())).thenAnswer(invocation -> {
            Supplier<?> s = (Supplier<?>) invocation.getArgument(0);
            return s.get();
        });

        Supplier<String> throwingSupplier = () -> {
            throw new IllegalStateException("boom");
        };
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> container.record(throwingSupplier, metricName, tags));
        assertEquals("boom", ex.getMessage());

        // verify the timer's record was invoked with the supplier
        verify(timer).record((Supplier<String>) throwingSupplier);
    }


}
