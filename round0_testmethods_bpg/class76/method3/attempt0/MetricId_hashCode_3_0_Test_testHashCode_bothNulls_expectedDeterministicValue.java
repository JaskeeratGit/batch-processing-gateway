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

public class MetricId_hashCode_3_0_Test_testHashCode_bothNulls_expectedDeterministicValue {

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






}
