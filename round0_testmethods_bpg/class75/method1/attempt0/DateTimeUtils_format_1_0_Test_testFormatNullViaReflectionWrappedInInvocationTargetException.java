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

public class DateTimeUtils_format_1_0_Test_testFormatNullViaReflectionWrappedInInvocationTargetException {





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

}
