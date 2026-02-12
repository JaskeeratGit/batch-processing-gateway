package com.apple.spark.util;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * JUnit 5 tests for TimerMetricContainer#record(Supplier, String, Collection)
 *
 * Uses Mockito to mock MeterRegistry and Timer.
 */
@ExtendWith(MockitoExtension.class)
class TimerMetricContainer_record_3_0_Test_record_shouldDelegateToTimerAndReturnValue {

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Timer timer;

    // We'll create a spy for the container so we can stub getTimer(...) to return our mocked Timer.
    private TimerMetricContainer container;

    @BeforeEach
    void setUp() {
        // Some MeterRegistry implementations may expose a config object. Return a mock config
        // to avoid potential NPEs during construction/use of TimerMetricContainer.
        MeterRegistry.Config mockConfig = mock(MeterRegistry.Config.class);
        when(meterRegistry.config()).thenReturn(mockConfig);

        // Create a spy so we can override getTimer(...) to return our mocked Timer regardless of how
        // TimerMetricContainer internally creates/registries the timer.
        container = spy(new TimerMetricContainer(meterRegistry));
    }

    @Test
    void record_shouldDelegateToTimerAndReturnValue() {
        String metricName = "test.metric";

        // Use simple concrete Tag implementations to avoid Mockito-created tags returning null for getKey()/getValue()
        Tag t1 = new Tag() {
            @Override
            public String getKey() {
                return "k1";
            }

            @Override
            public String getValue() {
                return "v1";
            }
        };
        Tag t2 = new Tag() {
            @Override
            public String getKey() {
                return "k2";
            }

            @Override
            public String getValue() {
                return "v2";
            }
        };
        Collection<Tag> tags = Arrays.asList(t1, t2);

        // Make the spy's getTimer(...) return our mocked timer regardless of how the container would obtain it.
        // Using doReturn to avoid calling the real getTimer implementation.
        doReturn(timer).when(container).getTimer(eq(metricName), eq(tags));

        // Disambiguate overloaded Timer.record(...) by specifying the Supplier type matcher
        when(timer.record(ArgumentMatchers.<Supplier<?>>any())).thenAnswer((InvocationOnMock invocation) -> {
            @SuppressWarnings("unchecked")
            Supplier<?> supplied = (Supplier<?>) invocation.getArgument(0);
            return supplied.get();
        });

        Supplier<String> supplier = () -> "ok-value";
        String result = container.record(supplier, metricName, tags);
        assertEquals("ok-value", result);

        // Verify that container asked for the timer and delegated to its record(...) method.
        verify(container).getTimer(metricName, tags);
        @SuppressWarnings("unchecked")
        Supplier<String> castSupplier = (Supplier<String>) supplier;
        verify(timer).record(castSupplier);
    }

}
