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

public class ExceptionUtils_meterIOException_4_0_Test_testMeterIOExceptionWhenNullThrowsNPE {

    private Field ioField;

    private Field modifiersField;

    private int originalModifiers;

    private Object originalValue;

    @BeforeEach
    void setUp() throws Exception {
        ioField = ExceptionUtils.class.getDeclaredField("ioExceptionMeter");
        ioField.setAccessible(true);
        originalValue = ioField.get(null);
        originalModifiers = ioField.getModifiers();
        // Field.modifiers is needed to remove the FINAL flag at runtime
        modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
    }

    @AfterEach
    void tearDown() throws Exception {
        // restore original modifiers and original value
        modifiersField.setInt(ioField, originalModifiers);
        ioField.set(null, originalValue);
    }


    @Test
    void testMeterIOExceptionWhenNullThrowsNPE() throws Exception {
        // remove final modifier and set the field to null to simulate missing meter
        modifiersField.setInt(ioField, ioField.getModifiers() & ~Modifier.FINAL);
        ioField.set(null, null);
        // Expect a NullPointerException because the method calls ioExceptionMeter.mark()
        assertThrows(NullPointerException.class, () -> ExceptionUtils.meterIOException());
    }
}
