package com.apple.spark.core;

import io.micrometer.statsd.StatsdFlavor;
import java.lang.reflect.Method;
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
import io.micrometer.statsd.StatsdMeterRegistry;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BPGStatsdConfig_flavor_3_0_Test_testFlavorReturnsDatadog_Reflection {


    @Test
    public void testFlavorReturnsDatadog_Reflection() throws Exception {
        BPGStatsdConfig config = new BPGStatsdConfig();
        // reflective invocation (useful if method visibility changes or to satisfy reflection requirement)
        Method flavorMethod = BPGStatsdConfig.class.getDeclaredMethod("flavor");
        flavorMethod.setAccessible(true);
        Object result = flavorMethod.invoke(config);
        assertNotNull(result, "reflective flavor() invocation should not return null");
        assertEquals(StatsdFlavor.DATADOG, result, "reflective flavor() invocation should return DATADOG");
        assertSame(StatsdFlavor.DATADOG, result, "reflective flavor() invocation should return the DATADOG enum instance");
    }
}
