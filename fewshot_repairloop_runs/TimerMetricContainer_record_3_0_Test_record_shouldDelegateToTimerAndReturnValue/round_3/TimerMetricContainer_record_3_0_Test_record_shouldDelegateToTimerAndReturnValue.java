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
 * Uses reflection to invoke the private getTimer(String, Collection<Tag>) method.
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
        // Ensure MeterRegistry.getConfig() returns a non-null config to avoid NPEs during Timer construction
        MeterRegistry.Config config = mock(MeterRegistry.Config.class);
        // Attempt to mock nested PauseDetector if present to avoid further NPEs when accessed
        try {
            MeterRegistry.Config.PauseDetector pd = mock(MeterRegistry.Config.PauseDetector.class);
            when(config.pauseDetector()).thenReturn(pd);
        } catch (Exception ignore) {
            // If the nested PauseDetector type is not present in this micrometer version, ignore.
        }
        // Some micrometer versions expose getConfig(); others expose config(). Try both safely.
        try {
            when(meterRegistry.getConfig()).thenReturn(config);
        } catch (Exception ignore) {
            try {
                when(meterRegistry.config()).thenReturn(config);
            } catch (Exception ignore2) {
                // If neither method is present, continue; the test will stub meterRegistry.timer directly.
            }
        }

        container = new TimerMetricContainer(meterRegistry);
    }

    @Test
    void record_shouldDelegateToTimerAndReturnValue() {
        String metricName = "test.metric";
        Tag t1 = mock(Tag.class);
        Tag t2 = mock(Tag.class);
        // Provide non-null keys/values to avoid NPEs inside metric id/key handling
        when(t1.getKey()).thenReturn("k1");
        when(t1.getValue()).thenReturn("v1");
        when(t2.getKey()).thenReturn("k2");
        when(t2.getValue()).thenReturn("v2");

        Collection<Tag> tags = Arrays.asList(t1, t2);
        // Make meterRegistry.timer(...) return our mocked timer. Use anyCollection() to avoid Tag equality NPE.
        when(meterRegistry.timer(ArgumentMatchers.eq(metricName), ArgumentMatchers.anyCollection())).thenReturn(timer);
        // Make timer.record call the supplied Supplier so we exercise supplier logic and return its value.
        when(timer.record((Supplier<?>) ArgumentMatchers.any())).thenAnswer(invocation -> {
            Supplier<?> supplied = (Supplier<?>) invocation.getArgument(0);
            return supplied.get();
        });
        Supplier<String> supplier = () -> "ok-value";
        String result = container.record(supplier, metricName, tags);
        assertEquals("ok-value", result);
        verify(meterRegistry).timer(ArgumentMatchers.eq(metricName), ArgumentMatchers.anyCollection());
        verify(timer).record(supplier);
    }

}
