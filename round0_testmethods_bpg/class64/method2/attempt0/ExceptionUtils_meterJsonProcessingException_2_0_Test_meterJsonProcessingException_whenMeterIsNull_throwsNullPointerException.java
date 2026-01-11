package com.apple.spark.util;

import com.codahale.metrics.Meter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.apple.spark.core.Constants;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;

public class ExceptionUtils_meterJsonProcessingException_2_0_Test_meterJsonProcessingException_whenMeterIsNull_throwsNullPointerException {

    private Field jsonMeterField;

    private Object originalJsonMeter;

    @BeforeEach
    void setUp() throws Exception {
        // Locate the private static final field
        jsonMeterField = ExceptionUtils.class.getDeclaredField("jsonProcessingExceptionMeter");
        jsonMeterField.setAccessible(true);
        // Save original value to restore later
        originalJsonMeter = jsonMeterField.get(null);
        // Replace with a fresh Meter instance for predictable behavior
        setFinalStatic(jsonMeterField, new Meter());
    }

    @AfterEach
    void tearDown() throws Exception {
        // Restore original value (may be null)
        setFinalStatic(jsonMeterField, originalJsonMeter);
    }


    @Test
    void meterJsonProcessingException_whenMeterIsNull_throwsNullPointerException() throws Exception {
        // Set the private static final field to null to simulate a missing meter
        setFinalStatic(jsonMeterField, null);
        // Expect a NullPointerException when the method tries to call mark() on null
        assertThrows(NullPointerException.class, ExceptionUtils::meterJsonProcessingException);
    }

    /**
     * Helper to set a private static final field via reflection.
     */
    private static void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);
        // Remove final modifier from the field
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        // Set new value (null for static fields uses null as target)
        field.set(null, newValue);
    }
}
