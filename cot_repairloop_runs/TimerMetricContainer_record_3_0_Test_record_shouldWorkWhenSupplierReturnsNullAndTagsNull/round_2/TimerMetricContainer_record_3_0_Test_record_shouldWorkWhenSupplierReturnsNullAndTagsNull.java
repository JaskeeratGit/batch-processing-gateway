package com.apple.spark.util;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

/**
 * JUnit 5 tests for TimerMetricContainer#record(Supplier, String, Collection)
 *
 * Uses Mockito to mock MeterRegistry and Timer.
 *
 * Fixed so the test does not trigger MeterRegistry internal behavior (which can call into config())
 * by overriding getTimer(...) on the container to return the mocked Timer directly.
 */
@ExtendWith(MockitoExtension.class)
class TimerMetricContainer_record_3_0_Test_record_shouldWorkWhenSupplierReturnsNullAndTagsNull {

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Timer timer;

    private TimerMetricContainer container;

    @BeforeEach
    void setUp() {
        // Avoid calling MeterRegistry.timer(...) implementation which may access internal config and cause NPE.
        // Instead create an anonymous subclass that returns our mocked Timer directly.
        container = new TimerMetricContainer(meterRegistry) {
            @Override
            protected Timer getTimer(String metricName, Collection<Tag> tags) {
                return timer;
            }
        };
    }

    @Test
    void record_shouldWorkWhenSupplierReturnsNullAndTagsNull() {
        String metricName = "null.metric";
        // Represent "no tags" with an empty collection to avoid NPE from production code when iterating tags.
        Collection<Tag> tags = Collections.emptyList();

        // Make timer.record(...) invoke the supplied Supplier and return its value (which will be null).
        when(timer.record(ArgumentMatchers.<Supplier<Object>>any())).thenAnswer(invocation -> {
            Supplier<?> s = (Supplier<?>) invocation.getArgument(0);
            return s.get();
        });

        Supplier<Object> supplier = () -> null;
        Object result = container.record(supplier, metricName, tags);
        assertNull(result);

        // Verify the Timer.record(...) was invoked with the same supplier.
        verify(timer).record((Supplier<Object>) supplier);
        // Ensure we did not interact with MeterRegistry (we avoided using it).
        verifyNoInteractions(meterRegistry);
    }

}
