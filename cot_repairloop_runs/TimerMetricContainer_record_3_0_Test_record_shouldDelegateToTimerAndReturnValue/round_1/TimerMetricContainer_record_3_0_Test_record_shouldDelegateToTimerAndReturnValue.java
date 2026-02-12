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
import org.mockito.invocation.InvocationOnMock;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

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

    private TimerMetricContainer container;

    @BeforeEach
    void setUp() {
        container = new TimerMetricContainer(meterRegistry);
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

        // Make meterRegistry.timer(...) return our mocked timer
        when(meterRegistry.timer(eq(metricName), eq(tags))).thenReturn(timer);

        // Disambiguate overloaded Timer.record(...) by specifying the Supplier type matcher
        when(timer.record(ArgumentMatchers.<Supplier<?>>any())).thenAnswer((InvocationOnMock invocation) -> {
            @SuppressWarnings("unchecked")
            Supplier<?> supplied = (Supplier<?>) invocation.getArgument(0);
            return supplied.get();
        });

        Supplier<String> supplier = () -> "ok-value";
        String result = container.record(supplier, metricName, tags);
        assertEquals("ok-value", result);

        verify(meterRegistry).timer(metricName, tags);
        // verify supplier overload called; cast to Supplier to avoid any potential overload ambiguity at compile time
        @SuppressWarnings("unchecked")
        Supplier<String> castSupplier = (Supplier<String>) supplier;
        verify(timer).record(castSupplier);
    }

}
