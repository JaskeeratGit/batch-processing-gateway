package com.apple.spark.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import org.slf4j.Logger;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import org.slf4j.LoggerFactory;

public class DateTimeUtils_format_1_0_Test {

    @Test
    public void testFormatDirectEpochZero() {
        Instant instant = Instant.ofEpochSecond(0L);
        String formatted = DateTimeUtils.format(instant);
        assertEquals("1970-01-01T00:00:00Z", formatted);
    }

    @Test
    public void testFormatDirectWithNanos() {
        Instant instant = Instant.ofEpochSecond(1L, 123456789L);
        String formatted = DateTimeUtils.format(instant);
        assertEquals("1970-01-01T00:00:01.123456789Z", formatted);
    }

    @Test
    public void testFormatDirectNullThrowsNPE() {
        assertThrows(NullPointerException.class, () -> DateTimeUtils.format(null));
    }

    @Test
    public void testFormatViaReflection() throws Exception {
        Class<?> cls = Class.forName("com.apple.spark.util.DateTimeUtils");
        Method formatMethod = cls.getMethod("format", Instant.class);
        String result = (String) formatMethod.invoke(null, Instant.ofEpochSecond(0L));
        assertEquals("1970-01-01T00:00:00Z", result);
    }

    @Test
    public void testFormatNullViaReflectionWrappedInInvocationTargetException() throws Exception {
        Class<?> cls = Class.forName("com.apple.spark.util.DateTimeUtils");
        Method formatMethod = cls.getMethod("format", Instant.class);
        InvocationTargetException ite = assertThrows(InvocationTargetException.class, () -> {
            // passing null via Object[] to ensure reflection treats it as an argument
            formatMethod.invoke(null, new Object[] { null });
        });
        // underlying cause should be NullPointerException
        assertTrue(ite.getCause() instanceof NullPointerException);
    }

    @Test
    public void testPrivateLoggerFieldAccessibleViaReflection() throws Exception {
        Field loggerField = DateTimeUtils.class.getDeclaredField("logger");
        loggerField.setAccessible(true);
        // static field
        Object loggerObj = loggerField.get(null);
        assertNotNull(loggerObj);
        assertTrue(Logger.class.isInstance(loggerObj));
    }
}
