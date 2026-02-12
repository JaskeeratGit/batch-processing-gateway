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

/**
 * JUnit 5 tests for TimerMetricContainer#record(Supplier, String, Collection)
 *
 * Uses Mockito to mock MeterRegistry and Timer.
 * Uses real Tag instances (via Tag.of) so Tag.getKey()/getValue() are non-null.
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
    void record_shouldDelegateToTimerAndReturnValue() {
        String metricName = "test.metric";
        Tag t1 = Tag.of("k1", "v1");
        Tag t2 = Tag.of("k2", "v2");
        Collection<Tag> tags = Arrays.asList(t1, t2);
        // Make meterRegistry.timer(...) return our mocked timer
        when(meterRegistry.timer(eq(metricName), eq(tags))).thenReturn(timer);
        // Make timer.record call the supplied Supplier so we exercise supplier logic and return its value.
        when(timer.record(ArgumentMatchers.<Supplier<?>>any())).thenAnswer(invocation -> {
            Supplier<?> supplied = (Supplier<?>) invocation.getArgument(0);
            // noinspection unchecked
            return supplied.get();
        });
        Supplier<String> supplier = () -> "ok-value";
        String result = container.record(supplier, metricName, tags);
        assertEquals("ok-value", result);
        verify(meterRegistry).timer(metricName, tags);
        verify(timer).record((Supplier<?>) supplier);
    }

}
