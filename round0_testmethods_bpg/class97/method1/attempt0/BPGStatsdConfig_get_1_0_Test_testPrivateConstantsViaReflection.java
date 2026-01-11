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

public class BPGStatsdConfig_get_1_0_Test_testPrivateConstantsViaReflection {

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

}
