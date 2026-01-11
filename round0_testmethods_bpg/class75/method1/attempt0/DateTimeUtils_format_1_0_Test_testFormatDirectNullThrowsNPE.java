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

public class DateTimeUtils_format_1_0_Test_testFormatDirectNullThrowsNPE {



    @Test
    public void testFormatDirectNullThrowsNPE() {
        assertThrows(NullPointerException.class, () -> DateTimeUtils.format(null));
    }



}
