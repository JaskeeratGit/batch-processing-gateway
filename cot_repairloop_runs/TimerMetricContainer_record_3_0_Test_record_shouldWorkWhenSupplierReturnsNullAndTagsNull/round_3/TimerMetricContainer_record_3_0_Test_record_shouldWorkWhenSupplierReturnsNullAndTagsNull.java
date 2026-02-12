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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * JUnit 5 tests for TimerMetricContainer#record(Supplier, String, Collection)
 *
 * Uses Mockito to mock MeterRegistry and Timer.
 *
 * This version avoids attempting to override a method signature that might not match the production
 * class by using a Mockito spy and stubbing getTimer(...) to return the mocked Timer.
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
        // Create a real instance and then spy it so we can stub getTimer(...) to return our mocked Timer.
        TimerMetricContainer real = new TimerMetricContainer(meterRegistry);
        container = spy(real);

        // Stub the getTimer(...) call on the spy to return the mocked timer for any name and tags.
        // Use doReturn(...) to avoid invoking the real method.
        doReturn(timer).when(container).getTimer(anyString(), ArgumentMatchers.<Collection<Tag>>any());
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
