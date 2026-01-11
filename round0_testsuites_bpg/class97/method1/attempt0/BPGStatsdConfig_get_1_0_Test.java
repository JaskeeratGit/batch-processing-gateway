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

public class BPGStatsdConfig_get_1_0_Test {

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
    void testGetReturnsNullForNullInput_directCall() {
        // direct call
        String result = config.get(null);
        assertNull(result, "Expected get(null) to return null");
    }

    @Test
    void testGetReturnsNullForNullInput_reflectionCall() throws Exception {
        // call via reflection (ensures reflective invocation works for this method)
        Method getMethod = BPGStatsdConfig.class.getDeclaredMethod("get", String.class);
        assertTrue(Modifier.isPublic(getMethod.getModifiers()), "get(String) should be public");
        Object reflectedResult = getMethod.invoke(config, new Object[] { (Object) null });
        assertNull(reflectedResult, "Reflected invocation of get(null) should return null");
    }

    @Test
    void testGetReturnsNullForVariousKeys() {
        // Several representative keys, including the private constant names
        String[] keys = new String[] { "", "someRandomKey", "STATSD_SERVER_IP", "STATSD_SERVER_PORT" };
        for (String key : keys) {
            String r = config.get(key);
            assertNull(r, "Expected get(\"" + key + "\") to return null");
        }
    }

    @Test
    void testPrivateConstantsViaReflection() throws Exception {
        // Access private static final String STATSD_SERVER_IP_ENV_NAME
        Field ipField = BPGStatsdConfig.class.getDeclaredField("STATSD_SERVER_IP_ENV_NAME");
        ipField.setAccessible(true);
        // static field
        Object ipValue = ipField.get(null);
        assertTrue(ipValue instanceof String, "STATSD_SERVER_IP_ENV_NAME must be a String");
        assertEquals("STATSD_SERVER_IP", ipValue);
        // Access private static final String STATSD_SERVER_PORT_ENV_NAME
        Field portField = BPGStatsdConfig.class.getDeclaredField("STATSD_SERVER_PORT_ENV_NAME");
        portField.setAccessible(true);
        // static field
        Object portValue = portField.get(null);
        assertTrue(portValue instanceof String, "STATSD_SERVER_PORT_ENV_NAME must be a String");
        assertEquals("STATSD_SERVER_PORT", portValue);
    }

    @Test
    void testLoggerFieldIsPresentAndIsLogger() throws Exception {
        Field loggerField = BPGStatsdConfig.class.getDeclaredField("logger");
        loggerField.setAccessible(true);
        // static logger
        Object loggerObj = loggerField.get(null);
        assertNotNull(loggerObj, "logger field should not be null");
        assertTrue(loggerObj instanceof Logger, "logger field should be an instance of org.slf4j.Logger");
    }
}
