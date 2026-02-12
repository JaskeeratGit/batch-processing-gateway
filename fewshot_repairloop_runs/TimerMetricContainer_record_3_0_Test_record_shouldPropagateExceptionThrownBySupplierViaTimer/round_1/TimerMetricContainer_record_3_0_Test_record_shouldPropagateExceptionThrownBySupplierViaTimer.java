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
        // Prevent MeterRegistry.config() from returning null (which would cause Timer.Builder.register to NPE)
        when(meterRegistry.config()).thenReturn(mock(MeterRegistry.Config.class, Mockito.RETURNS_DEEP_STUBS));
        container = new TimerMetricContainer(meterRegistry);
    }


    @Test
    void record_shouldPropagateExceptionThrownBySupplierViaTimer() {
        String metricName = "error.metric";
        Collection<Tag> tags = Collections.emptyList();

        Supplier<String> throwingSupplier = () -> {
            throw new IllegalStateException("boom");
        };

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> container.record(throwingSupplier, metricName, tags));
        assertEquals("boom", ex.getMessage());
    }


}
