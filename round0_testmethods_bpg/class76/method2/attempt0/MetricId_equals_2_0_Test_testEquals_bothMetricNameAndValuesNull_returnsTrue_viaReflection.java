package com.apple.spark.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

public class MetricId_equals_2_0_Test_testEquals_bothMetricNameAndValuesNull_returnsTrue_viaReflection {

    // Helper to set private (possibly final) fields via reflection
    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        // remove final modifier if present
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(target, value);
    }








    @Test
    public void testEquals_bothMetricNameAndValuesNull_returnsTrue_viaReflection() throws Exception {
        // Create two instances with non-null fields, then null out fields via reflection
        MetricId a = new MetricId("m", Arrays.asList("v"));
        MetricId b = new MetricId("m", Arrays.asList("v"));
        // set both metricName and values to null on both instances
        setPrivateField(a, "metricName", null);
        setPrivateField(a, "values", null);
        setPrivateField(b, "metricName", null);
        setPrivateField(b, "values", null);
        assertTrue(a.equals(b), "When both metricName and values are null on both instances, equals should return true");
    }

}
