package com.apple.spark.core;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.slf4j.Logger;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import io.micrometer.statsd.StatsdConfig;
import io.micrometer.statsd.StatsdFlavor;
import io.micrometer.statsd.StatsdMeterRegistry;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

public class BPGStatsdConfig_get_1_0_Test_testGetReturnsNullForNullInput_reflectionCall {

    private static BPGStatsdConfig config;

    @BeforeAll
    static void setup() {
        config = new BPGStatsdConfig();
    }

    @AfterAll
    static void teardown() {
        config = null;
    }


    @Test
    void testGetReturnsNullForNullInput_reflectionCall() throws Exception {
        // call via reflection (ensures reflective invocation works for this method)
        Method getMethod = BPGStatsdConfig.class.getDeclaredMethod("get", String.class);
        assertTrue(Modifier.isPublic(getMethod.getModifiers()), "get(String) should be public");
        Object reflectedResult = getMethod.invoke(config, new Object[] { (Object) null });
        assertNull(reflectedResult, "Reflected invocation of get(null) should return null");
    }



}
