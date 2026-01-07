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

public class MetricId_hashCode_3_0_Test_testHashCode_nullMetricName_nonNullValues {

    // Helper to invoke hashCode via reflection (to exercise reflection usage)
    private int invokeHashCodeViaReflection(MetricId metricId) throws Exception {
        Method hashCodeMethod = MetricId.class.getMethod("hashCode");
        Object result = hashCodeMethod.invoke(metricId);
        return ((Integer) result).intValue();
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


}
