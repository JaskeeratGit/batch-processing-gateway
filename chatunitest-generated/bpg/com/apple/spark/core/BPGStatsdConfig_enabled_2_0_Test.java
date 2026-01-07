package com.apple.spark.core;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
  JUnit 5 tests for BPGStatsdConfig.enabled()
  - Instead of trying to mutate System.getenv (which is blocked by the Java module system),
    these tests create anonymous subclasses of BPGStatsdConfig in the same package and
    override host() and port() to simulate different environment conditions.

  Note: host() and port() must be declared public to properly override the (public) methods
  from the StatsdConfig interface implemented by BPGStatsdConfig.
*/
class BPGStatsdConfig_enabled_2_0_Test {

    @Test
    void enabledTrueWhenHostNotEmptyAndPortPositive() {
        BPGStatsdConfig cfg = new BPGStatsdConfig() {

            @Override
            public String host() {
                return "1.2.3.4";
            }

            @Override
            public int port() {
                return 8125;
            }
        };
        assertTrue(cfg.enabled());
    }

    @Test
    void enabledFalseWhenHostEmpty() {
        BPGStatsdConfig cfg = new BPGStatsdConfig() {

            @Override
            public String host() {
                return "";
            }

            @Override
            public int port() {
                return 8125;
            }
        };
        assertFalse(cfg.enabled());
    }

    @Test
    void enabledFalseWhenHostMissing() {
        BPGStatsdConfig cfg = new BPGStatsdConfig() {

            @Override
            public String host() {
                return null;
            }

            @Override
            public int port() {
                return 8125;
            }
        };
        assertFalse(cfg.enabled());
    }

    @Test
    void enabledFalseWhenPortEmpty() {
        BPGStatsdConfig cfg = new BPGStatsdConfig() {

            @Override
            public String host() {
                return "1.2.3.4";
            }

            @Override
            public int port() {
                // simulate empty/invalid port parsing resulting in 0
                return 0;
            }
        };
        assertFalse(cfg.enabled());
    }

    @Test
    void enabledFalseWhenPortInvalid() {
        BPGStatsdConfig cfg = new BPGStatsdConfig() {

            @Override
            public String host() {
                return "1.2.3.4";
            }

            @Override
            public int port() {
                // simulate invalid port parsing resulting in 0
                return 0;
            }
        };
        assertFalse(cfg.enabled());
    }
}
