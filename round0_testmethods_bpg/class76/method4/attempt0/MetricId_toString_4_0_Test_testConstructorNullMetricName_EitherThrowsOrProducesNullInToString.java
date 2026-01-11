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

public class MetricId_toString_4_0_Test_testConstructorNullMetricName_EitherThrowsOrProducesNullInToString {




    @Test
    public void testConstructorNullMetricName_EitherThrowsOrProducesNullInToString() {
        try {
            MetricId metricId = new MetricId(null, Collections.emptyList());
            // If constructor allows null metricName, ensure toString reflects it (either as 'null' inside quotes)
            String ts = metricId.toString();
            assertTrue(ts.contains("metricName='null'") || ts.contains("metricName=null"), "toString should indicate a null metricName when constructed with null; actual: " + ts);
        } catch (NullPointerException npe) {
            // Acceptable behavior: constructor may throw NPE for null metricName
        }
    }

    // Helper to modify private final fields via reflection for testing purposes.
    private static void setFinalField(Object target, String fieldName, Object value) throws Exception {
        Field field = getDeclaredFieldIncludingSuperclasses(target.getClass(), fieldName);
        field.setAccessible(true);
        // Remove final modifier if present
        try {
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        } catch (NoSuchFieldException ignored) {
            // Some JVMs do not allow modification of the modifiers field; attempt to set anyway
        }
        field.set(target, value);
    }

    private static Field getDeclaredFieldIncludingSuperclasses(Class<?> clazz, String name) throws NoSuchFieldException {
        Class<?> current = clazz;
        while (current != null) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException("Field '" + name + "' not found in class hierarchy of " + clazz.getName());
    }
}
