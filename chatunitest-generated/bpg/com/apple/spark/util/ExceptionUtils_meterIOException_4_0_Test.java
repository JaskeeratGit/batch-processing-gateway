package com.apple.spark.util;

import com.codahale.metrics.Meter;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.apple.spark.core.Constants;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;

public class ExceptionUtils_meterIOException_4_0_Test {

    private Field ioField;

    private Object originalValue;

    private VarHandle ioVarHandle;

    @BeforeEach
    void setUp() throws Exception {
        ioField = ExceptionUtils.class.getDeclaredField("ioExceptionMeter");
        ioField.setAccessible(true);
        originalValue = ioField.get(null);
        // Try to obtain a VarHandle to allow modifying the static final field without relying on "modifiers" reflection hack
        try {
            ioVarHandle = MethodHandles.privateLookupIn(ExceptionUtils.class, MethodHandles.lookup()).findStaticVarHandle(ExceptionUtils.class, "ioExceptionMeter", Meter.class);
        } catch (Throwable t) {
            ioVarHandle = null;
        }
    }

    @Test
    void testMeterIOExceptionIncrementsMeter() throws Exception {
        Meter meter = (Meter) ioField.get(null);
        long before = meter.getCount();
        ExceptionUtils.meterIOException();
        long after = meter.getCount();
        assertEquals(before + 1, after);
    }
}
