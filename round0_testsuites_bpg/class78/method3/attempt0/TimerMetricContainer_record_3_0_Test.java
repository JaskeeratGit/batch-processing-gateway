package com.apple.spark.util;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * JUnit 5 tests for TimerMetricContainer#record(Supplier, String, Collection)
 *
 * Uses Mockito to mock MeterRegistry and Timer.
 * Uses reflection to invoke the private getTimer(String, Collection<Tag>) method.
 */
@ExtendWith(MockitoExtension.class)
class TimerMetricContainer_record_3_0_Test {

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
        Tag t1 = mock(Tag.class);
        Tag t2 = mock(Tag.class);
        Collection<Tag> tags = Arrays.asList(t1, t2);
        // Make meterRegistry.timer(...) return our mocked timer
        when(meterRegistry.timer(eq(metricName), eq(tags))).thenReturn(timer);
        // Make timer.record call the supplied Supplier so we exercise supplier logic and return its value.
        when(timer.record(ArgumentMatchers.any())).thenAnswer(invocation -> {
            Supplier<?> supplied = invocation.getArgument(0);
            // noinspection unchecked
            return supplied.get();
        });
        Supplier<String> supplier = () -> "ok-value";
        String result = container.record(supplier, metricName, tags);
        assertEquals("ok-value", result);
        verify(meterRegistry).timer(metricName, tags);
        verify(timer).record(supplier);
    }

    @Test
    void record_shouldPropagateExceptionThrownBySupplierViaTimer() {
        String metricName = "error.metric";
        Collection<Tag> tags = Collections.emptyList();
        when(meterRegistry.timer(eq(metricName), eq(tags))).thenReturn(timer);
        // timer.record will invoke the supplier and therefore throw
        when(timer.record(ArgumentMatchers.any())).thenAnswer(invocation -> {
            Supplier<?> s = invocation.getArgument(0);
            // will throw
            return s.get();
        });
        Supplier<String> throwingSupplier = () -> {
            throw new IllegalStateException("boom");
        };
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> container.record(throwingSupplier, metricName, tags));
        assertEquals("boom", ex.getMessage());
        verify(meterRegistry).timer(metricName, tags);
        verify(timer).record(throwingSupplier);
    }

    @Test
    void record_shouldWorkWhenSupplierReturnsNullAndTagsNull() {
        String metricName = "null.metric";
        // simulate null tags
        Collection<Tag> tags = null;
        when(meterRegistry.timer(eq(metricName), isNull())).thenReturn(timer);
        when(timer.record(ArgumentMatchers.any())).thenAnswer(invocation -> {
            Supplier<?> s = invocation.getArgument(0);
            return s.get();
        });
        Supplier<Object> supplier = () -> null;
        Object result = container.record(supplier, metricName, tags);
        assertNull(result);
        verify(meterRegistry).timer(metricName, null);
        verify(timer).record(supplier);
    }

    @Test
    void privateGetTimer_shouldReturnTimer_usingReflection() throws Exception {
        String metricName = "reflect.metric";
        Tag tag = mock(Tag.class);
        Collection<Tag> tags = Arrays.asList(tag);
        // Ensure meterRegistry.timer(...) is used by the private method (likely)
        when(meterRegistry.timer(eq(metricName), eq(tags))).thenReturn(timer);
        // Attempt to invoke private getTimer(String, Collection) via reflection.
        Method getTimerMethod = TimerMetricContainer.class.getDeclaredMethod("getTimer", String.class, Collection.class);
        getTimerMethod.setAccessible(true);
        Object returned = getTimerMethod.invoke(container, metricName, tags);
        assertSame(timer, returned);
        verify(meterRegistry).timer(metricName, tags);
    }
}
