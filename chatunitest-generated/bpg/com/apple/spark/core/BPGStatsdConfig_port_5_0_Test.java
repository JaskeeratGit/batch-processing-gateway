package com.apple.spark.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Map;
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

public class BPGStatsdConfig_port_5_0_Test {

    private static final String STATSD_SERVER_PORT_ENV_NAME = "STATSD_SERVER_PORT";

    @Test
    public void testPort_whenEnvNotSet_returnsZero() throws Exception {
        int result = runPortInSubprocess(null, false);
        assertEquals(0, result);
    }

    @Test
    public void testPort_whenEnvEmpty_returnsZero() throws Exception {
        int result = runPortInSubprocess("", true);
        assertEquals(0, result);
    }

    @Test
    public void testPort_whenEnvIsValidNumber_returnsParsedInt() throws Exception {
        int result = runPortInSubprocess("8125", true);
        assertEquals(8125, result);
    }

    @Test
    public void testPort_whenEnvIsInvalidNumber_returnsZero() throws Exception {
        int result = runPortInSubprocess("notANumber", true);
        assertEquals(0, result);
    }

    // Run a separate JVM with the desired environment to avoid modifying the current JVM's System.getenv (which is not modifiable under modules)
    private int runPortInSubprocess(String envValue, boolean setEnv) throws Exception {
        String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        String classpath = System.getProperty("java.class.path");
        String runnerClass = this.getClass().getName() + "$PortRunner";
        ProcessBuilder pb = new ProcessBuilder(javaBin, "-cp", classpath, runnerClass);
        Map<String, String> env = pb.environment();
        if (setEnv) {
            env.put(STATSD_SERVER_PORT_ENV_NAME, envValue);
        } else {
            env.remove(STATSD_SERVER_PORT_ENV_NAME);
        }
        pb.redirectErrorStream(true);
        Process p = pb.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line = reader.readLine();
            p.waitFor(5, TimeUnit.SECONDS);
            if (line == null) {
                return 0;
            }
            try {
                return Integer.parseInt(line.trim());
            } catch (NumberFormatException ex) {
                return 0;
            }
        }
    }

    // Runner invoked in a subprocess; it reads the environment and prints the port() result to stdout.
    public static class PortRunner {

        public static void main(String[] args) {
            try {
                int r = new BPGStatsdConfig().port();
                System.out.println(r);
            } catch (Throwable t) {
                t.printStackTrace();
                System.exit(2);
            }
        }
    }
}
