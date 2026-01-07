package com.apple.spark.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
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

public class BPGStatsdConfig_host_4_0_Test {

    // Helper main runner executed in a subprocess so we can control environment variables
    // The class is nested so it's compiled into the same classpath and accessible from the subprocess.
    public static class EnvPrinter {

        public static void main(String[] args) {
            BPGStatsdConfig config = new BPGStatsdConfig();
            String v = config.host();
            if (v == null) {
                System.out.print("__NULL__");
            } else {
                System.out.print(v);
            }
        }
    }

    @Test
    public void testHostReturnsEnvValue() throws Exception {
        Map<String, String> overrides = new HashMap<>();
        overrides.put("STATSD_SERVER_IP", "1.2.3.4");
        String out = runSubprocessWithEnv(overrides);
        assertEquals("1.2.3.4", out);
    }

    @Test
    public void testHostReturnsNullWhenEnvNotSet() throws Exception {
        Map<String, String> overrides = new HashMap<>();
        // indicate removal by mapping to null
        overrides.put("STATSD_SERVER_IP", null);
        String out = runSubprocessWithEnv(overrides);
        assertEquals("__NULL__", out);
    }

    @Test
    public void testHostReturnsEmptyStringWhenEnvEmpty() throws Exception {
        Map<String, String> overrides = new HashMap<>();
        overrides.put("STATSD_SERVER_IP", "");
        String out = runSubprocessWithEnv(overrides);
        assertEquals("", out);
    }

    private String runSubprocessWithEnv(Map<String, String> overrides) throws Exception {
        String javaCmd = "java";
        String classpath = System.getProperty("java.class.path");
        String mainClass = "com.apple.spark.core.BPGStatsdConfig_host_4_0_Test$EnvPrinter";
        ProcessBuilder pb = new ProcessBuilder(javaCmd, "-cp", classpath, mainClass);
        // Start with a copy of current environment, then apply overrides
        Map<String, String> env = pb.environment();
        env.clear();
        env.putAll(System.getenv());
        if (overrides != null) {
            for (Map.Entry<String, String> e : overrides.entrySet()) {
                if (e.getValue() == null) {
                    env.remove(e.getKey());
                } else {
                    env.put(e.getKey(), e.getValue());
                }
            }
        }
        Process p = pb.start();
        boolean finished = p.waitFor(5, TimeUnit.SECONDS);
        if (!finished) {
            p.destroyForcibly();
            throw new IllegalStateException("Subprocess did not finish in time");
        }
        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            StringBuilder sb = new StringBuilder();
            String line;
            // We used System.out.print in the subprocess, so there may be no newline.
            while ((line = r.readLine()) != null) {
                if (sb.length() > 0)
                    sb.append(System.lineSeparator());
                sb.append(line);
            }
            return sb.toString();
        }
    }
}
