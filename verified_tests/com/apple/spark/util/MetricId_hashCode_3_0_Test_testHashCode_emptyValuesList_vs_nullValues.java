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

public class MetricId_hashCode_3_0_Test_testHashCode_emptyValuesList_vs_nullValues {

    // Helper to invoke hashCode via reflection (to exercise reflection usage)
    private int invokeHashCodeViaReflection(MetricId metricId) throws Exception {
        Method hashCodeMethod = MetricId.class.getMethod("hashCode");
        Object result = hashCodeMethod.invoke(metricId);
        return ((Integer) result).intValue();
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
