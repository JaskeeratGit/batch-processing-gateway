package com.apple.spark.util;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for TimerMetricContainer#record(Supplier, String, Collection)
 *
 * This version uses a real SimpleMeterRegistry so the production private getTimer(...)
 * implementation can operate normally (no need to stub a private method).
 */
class TimerMetricContainer_record_3_0_Test_record_shouldWorkWhenSupplierReturnsNullAndTagsNull {

    private SimpleMeterRegistry meterRegistry;
    private TimerMetricContainer container;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        container = new TimerMetricContainer(meterRegistry);
    }

    @Test
    void record_shouldWorkWhenSupplierReturnsNullAndTagsNull() {
        String metricName = "null.metric";
        // Represent "no tags" with an empty collection to avoid NPE from production code when iterating tags.
        Collection<Tag> tags = Collections.emptyList();

        Supplier<Object> supplier = () -> null;
        Object result = container.record(supplier, metricName, tags);
        assertNull(result, "record(...) should return the supplier's result (null)");

        // Ensure a Timer with the given name was created/registered in the registry.
        Timer timer = meterRegistry.find(metricName).timer();
        assertNotNull(timer, "A Timer with the given metric name should have been registered in the registry");
    }
}
