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

public class MetricId_equals_2_0_Test_testEquals_sameContents_returnsTrue {

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
    public void testEquals_sameContents_returnsTrue() {
        List<String> vals1 = Arrays.asList("x", "y", "z");
        List<String> vals2 = Arrays.asList("x", "y", "z");
        MetricId m1 = new MetricId("myMetric", vals1);
        MetricId m2 = new MetricId("myMetric", vals2);
        assertTrue(m1.equals(m2), "Two MetricId with identical metricName and values should be equal");
        assertTrue(m2.equals(m1), "Equality should be symmetric");
    }





}
