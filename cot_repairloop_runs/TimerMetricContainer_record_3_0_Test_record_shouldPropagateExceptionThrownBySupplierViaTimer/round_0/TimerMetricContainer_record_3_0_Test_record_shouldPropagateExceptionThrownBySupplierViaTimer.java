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

/**
 * JUnit 5 tests for TimerMetricContainer#record(Supplier, String, Collection)
 *
 * Uses Mockito to mock MeterRegistry and Timer.
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
    void record_shouldPropagateExceptionThrownBySupplierViaTimer() {
        String metricName = "error.metric";
        Collection<Tag> tags = Collections.emptyList();
        when(meterRegistry.timer(eq(metricName), eq(tags))).thenReturn(timer);

        // Disambiguate overloaded Timer.record by specifying Supplier type in the matcher
        when(timer.record(ArgumentMatchers.<Supplier<?>>any())).thenAnswer(invocation -> {
            // use typed getArgument to avoid unchecked warnings
            Supplier<?> s = invocation.getArgument(0, Supplier.class);
            // invoke supplier (this will throw for the test)
            return s.get();
        });

        Supplier<String> throwingSupplier = () -> {
            throw new IllegalStateException("boom");
        };

        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> container.record(throwingSupplier, metricName, tags));
        assertEquals("boom", ex.getMessage());

        verify(meterRegistry).timer(metricName, tags);
        // Disambiguate overloaded verify by providing a typed eq() to guide resolution
        verify(timer).record(Mockito.<Supplier<String>>eq(throwingSupplier));
    }

}
