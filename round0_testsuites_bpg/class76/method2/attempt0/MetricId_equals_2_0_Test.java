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

public class MetricId_equals_2_0_Test {

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
    public void testEquals_sameReference_returnsTrue() {
        MetricId m = new MetricId("metric", Arrays.asList("a", "b"));
        assertTrue(m.equals(m), "An object should be equal to itself (this == o)");
    }

    @Test
    public void testEquals_null_returnsFalse() {
        MetricId m = new MetricId("metric", Arrays.asList("a"));
        assertFalse(m.equals(null), "Comparing to null should return false");
    }

    @Test
    public void testEquals_differentClass_returnsFalse() {
        MetricId base = new MetricId("metric", Arrays.asList("a"));
        // anonymous subclass -> different getClass()
        MetricId subclassInstance = new MetricId("metric", Arrays.asList("a")) {
        };
        assertFalse(base.equals(subclassInstance), "Instances of different runtime classes should not be equal");
        assertFalse(subclassInstance.equals(base), "Symmetry: subclass instance should not equal base instance");
    }

    @Test
    public void testEquals_sameContents_returnsTrue() {
        List<String> vals1 = Arrays.asList("x", "y", "z");
        List<String> vals2 = Arrays.asList("x", "y", "z");
        MetricId m1 = new MetricId("myMetric", vals1);
        MetricId m2 = new MetricId("myMetric", vals2);
        assertTrue(m1.equals(m2), "Two MetricId with identical metricName and values should be equal");
        assertTrue(m2.equals(m1), "Equality should be symmetric");
    }

    @Test
    public void testEquals_metricNameDifferent_returnsFalse() {
        List<String> vals = Arrays.asList("a");
        MetricId m1 = new MetricId("metricA", vals);
        MetricId m2 = new MetricId("metricB", vals);
        assertFalse(m1.equals(m2), "Different metricName with same values should not be equal");
    }

    @Test
    public void testEquals_valuesDifferent_returnsFalse() {
        MetricId m1 = new MetricId("metric", Arrays.asList("a", "b"));
        MetricId m2 = new MetricId("metric", Arrays.asList("a", "c"));
        assertFalse(m1.equals(m2), "Same metricName but different values should not be equal");
    }

    @Test
    public void testEquals_valuesOrderMatters_returnsFalse() {
        MetricId m1 = new MetricId("metric", Arrays.asList("a", "b"));
        MetricId m2 = new MetricId("metric", Arrays.asList("b", "a"));
        assertFalse(m1.equals(m2), "Order of values should matter for list equality");
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

    @Test
    public void testEquals_oneValuesNull_otherEmptyList_returnsFalse_viaReflection() throws Exception {
        MetricId a = new MetricId("m", Arrays.asList("v"));
        MetricId b = new MetricId("m", Collections.emptyList());
        // set values of 'a' to null while 'b' has empty list
        setPrivateField(a, "values", null);
        assertFalse(a.equals(b), "null values should not equal an empty list");
        assertFalse(b.equals(a), "Symmetry: empty list should not equal null values");
    }
}
