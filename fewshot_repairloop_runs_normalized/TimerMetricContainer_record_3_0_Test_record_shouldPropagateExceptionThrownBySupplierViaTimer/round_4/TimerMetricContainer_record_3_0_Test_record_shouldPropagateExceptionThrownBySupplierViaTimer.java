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
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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

        // Ensure the container will return our mocked timer without invoking Timer.builder.register(...)
        // by pre-populating the private timers map with a MetricId equal to the one computed inside getTimer.
        Class<?> metricIdClass = Class.forName("com.apple.spark.util.TimerMetricContainer$MetricId");
        Constructor<?> ctor = metricIdClass.getDeclaredConstructor(String.class, Collection.class);
        ctor.setAccessible(true);
        Object metricId = ctor.newInstance(metricName, tags);

        Field timersField = TimerMetricContainer.class.getDeclaredField("timers");
        timersField.setAccessible(true);
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<Object, Timer> timers = (ConcurrentHashMap<Object, Timer>) timersField.get(container);
        timers.put(metricId, timer);

        // timer.record will invoke the supplier and therefore throw
        when(timer.record(ArgumentMatchers.<Supplier<?>>any())).thenAnswer(invocation -> {
            Supplier<?> s = invocation.getArgument(0);
            // will throw
            return s.get();
        });

        Supplier<String> throwingSupplier = () -> {
            throw new IllegalStateException("boom");
        };

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> container.record(throwingSupplier, metricName, tags));
        assertEquals("boom", ex.getMessage());
        verify(timer).record(throwingSupplier);
    }


}
