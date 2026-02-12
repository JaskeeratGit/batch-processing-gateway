package com.apple.spark.util;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Collection;
import java.util.function.Supplier;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for TimerMetricContainer#record(Supplier, String, Collection)
 *
 * Uses Mockito to mock MeterRegistry and Timer.
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
        container = new TimerMetricContainer(meterRegistry);
    }

    @Test
    void record_shouldWorkWhenSupplierReturnsNullAndTagsNull() {
        String metricName = "null.metric";
        // simulate null tags
        Collection<Tag> tags = null;

        // Disambiguate the overloaded MeterRegistry.timer(...) by forcing the iterable<Tag> matcher
        when(meterRegistry.timer(eq(metricName), Mockito.<Iterable<Tag>>isNull())).thenReturn(timer);

        // Disambiguate the overloaded Timer.record(...) by forcing the Supplier<?> matcher
        when(timer.record(ArgumentMatchers.<Supplier<Object>>any())).thenAnswer(invocation -> {
            Supplier<?> s = (Supplier<?>) invocation.getArgument(0);
            return s.get();
        });

        Supplier<Object> supplier = () -> null;
        Object result = container.record(supplier, metricName, tags);
        assertNull(result);

        // Verify calls - disambiguate overloaded timer(...) verification similarly
        verify(meterRegistry).timer(eq(metricName), Mockito.<Iterable<Tag>>isNull());
        // Disambiguate the Timer.record(...) verification by casting to Supplier<Object>
        verify(timer).record((Supplier<Object>) supplier);
    }

}
