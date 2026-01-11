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

public class BPGStatsdConfig_flavor_3_0_Test_testFlavorReturnsDatadog_DirectCall {

    @Test
    public void testFlavorReturnsDatadog_DirectCall() {
        BPGStatsdConfig config = new BPGStatsdConfig();
        // direct invocation
        StatsdFlavor flavor = config.flavor();
        assertNotNull(flavor, "flavor() should not return null");
        assertEquals(StatsdFlavor.DATADOG, flavor, "flavor() should return DATADOG");
        // same enum constant instance
        assertSame(StatsdFlavor.DATADOG, flavor, "flavor() should return the DATADOG enum instance");
    }

}
