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

public class MetricId_equals_2_0_Test_testEquals_sameReference_returnsTrue {

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








}
