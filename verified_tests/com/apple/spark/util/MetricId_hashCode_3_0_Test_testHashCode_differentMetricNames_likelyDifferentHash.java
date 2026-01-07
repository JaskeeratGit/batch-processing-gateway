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

public class MetricId_hashCode_3_0_Test_testHashCode_differentMetricNames_likelyDifferentHash {

    // Helper to invoke hashCode via reflection (to exercise reflection usage)
    private int invokeHashCodeViaReflection(MetricId metricId) throws Exception {
        Method hashCodeMethod = MetricId.class.getMethod("hashCode");
        Object result = hashCodeMethod.invoke(metricId);
        return ((Integer) result).intValue();
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



}
