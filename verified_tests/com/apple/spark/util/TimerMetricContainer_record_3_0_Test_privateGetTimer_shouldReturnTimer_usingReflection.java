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
class TimerMetricContainer_record_3_0_Test_privateGetTimer_shouldReturnTimer_usingReflection {

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
