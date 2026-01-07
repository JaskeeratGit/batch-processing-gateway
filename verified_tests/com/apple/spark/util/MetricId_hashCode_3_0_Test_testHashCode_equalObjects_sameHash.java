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

public class MetricId_hashCode_3_0_Test_testHashCode_equalObjects_sameHash {

    // Helper to invoke hashCode via reflection (to exercise reflection usage)
    private int invokeHashCodeViaReflection(MetricId metricId) throws Exception {
        Method hashCodeMethod = MetricId.class.getMethod("hashCode");
        Object result = hashCodeMethod.invoke(metricId);
        return ((Integer) result).intValue();
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




}
