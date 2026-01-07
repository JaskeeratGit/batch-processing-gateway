package com.apple.spark.util;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Collection;
import java.util.stream.Collectors;

public class MetricId_hashCode_3_0_Test {

    // Helper to invoke hashCode via reflection (to exercise reflection usage)
    private int invokeHashCodeViaReflection(MetricId metricId) throws Exception {
        Method hashCodeMethod = MetricId.class.getMethod("hashCode");
        Object result = hashCodeMethod.invoke(metricId);
        return ((Integer) result).intValue();
    }

    @Test
    public void testHashCode_bothNulls_expectedDeterministicValue() throws Exception {
        MetricId m = new MetricId(null, null);
        int hash = invokeHashCodeViaReflection(m);
        // Objects.hash(null, null) = Arrays.hashCode(new Object[]{null, null})
        // Computed: result starts at 1 -> 31*1 + 0 = 31 -> 31*31 + 0 = 961
        assertEquals(961, hash);
    }

    @Test
    public void testHashCode_nonNullFields_matchesObjectsHash() throws Exception {
        String metricName = "metricX";
        List<String> values = Arrays.asList("a", "b", "c");
        MetricId m = new MetricId(metricName, values);
        int actual = invokeHashCodeViaReflection(m);
        int expected = Objects.hash(metricName, values);
        assertEquals(expected, actual);
    }

    @Test
    public void testHashCode_equalObjects_sameHash() throws Exception {
        String metricName = "sameMetric";
        List<String> values1 = Arrays.asList("one", "two");
        // distinct instance but equal contents
        List<String> values2 = Arrays.asList("one", "two");
        MetricId m1 = new MetricId(metricName, values1);
        MetricId m2 = new MetricId(metricName, values2);
        int h1 = invokeHashCodeViaReflection(m1);
        int h2 = invokeHashCodeViaReflection(m2);
        assertEquals(h1, h2);
    }

    @Test
    public void testHashCode_differentMetricNames_likelyDifferentHash() throws Exception {
        String metricName1 = "metricA";
        String metricName2 = "metricB";
        List<String> values = Arrays.asList("x");
        MetricId m1 = new MetricId(metricName1, values);
        MetricId m2 = new MetricId(metricName2, values);
        int h1 = invokeHashCodeViaReflection(m1);
        int h2 = invokeHashCodeViaReflection(m2);
        // Very unlikely collision for simple different strings; assert difference to cover branch of differing fields
        assertNotEquals(h1, h2);
    }

    @Test
    public void testHashCode_nullMetricName_nonNullValues() throws Exception {
        String metricName = null;
        List<String> values = Arrays.asList("p", "q");
        MetricId m = new MetricId(metricName, values);
        int actual = invokeHashCodeViaReflection(m);
        int expected = Objects.hash(metricName, values);
        assertEquals(expected, actual);
    }

    @Test
    public void testHashCode_nonNullMetricName_nullValues() throws Exception {
        String metricName = "onlyName";
        List<String> values = null;
        MetricId m = new MetricId(metricName, values);
        int actual = invokeHashCodeViaReflection(m);
        int expected = Objects.hash(metricName, values);
        assertEquals(expected, actual);
    }

    @Test
    public void testHashCode_emptyValuesList_vs_nullValues() throws Exception {
        String metricName = "mEmpty";
        List<String> emptyList = Collections.emptyList();
        MetricId withEmpty = new MetricId(metricName, emptyList);
        MetricId withNull = new MetricId(metricName, null);
        int hEmpty = invokeHashCodeViaReflection(withEmpty);
        int hNull = invokeHashCodeViaReflection(withNull);
        // empty list and null produce different hash codes (empty list hashCode != 0)
        assertNotEquals(hEmpty, hNull);
    }
}
