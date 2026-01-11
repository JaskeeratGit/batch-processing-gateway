package com.apple.spark.core;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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

    private static final String ENV_NAME = "STATSD_SERVER_IP";

    private Map<String, String> originalEnv;

    @BeforeEach
    public void saveOriginalEnv() {
        originalEnv = new HashMap<>(System.getenv());
    }

    @AfterEach
    public void restoreOriginalEnv() throws Exception {
        setEnv(originalEnv);
    }

    @Test
    public void testHostReturnsEnvValue() throws Exception {
        // arrange
        Map<String, String> modified = new HashMap<>(originalEnv);
        modified.put(ENV_NAME, "1.2.3.4");
        setEnv(modified);
        // act (invoke via reflection)
        Method hostMethod = BPGStatsdConfig.class.getDeclaredMethod("host");
        hostMethod.setAccessible(true);
        Object returned = hostMethod.invoke(new BPGStatsdConfig());
        // assert
        assertTrue(returned instanceof String);
        assertEquals("1.2.3.4", returned);
    }

    @Test
    public void testHostReturnsNullWhenEnvNotSet() throws Exception {
        // arrange: ensure env var is removed
        Map<String, String> modified = new HashMap<>(originalEnv);
        modified.remove(ENV_NAME);
        setEnv(modified);
        // act
        Method hostMethod = BPGStatsdConfig.class.getDeclaredMethod("host");
        hostMethod.setAccessible(true);
        Object returned = hostMethod.invoke(new BPGStatsdConfig());
        // assert
        assertNull(returned);
    }

    @Test
    public void testHostReturnsEmptyStringWhenEnvEmpty() throws Exception {
        // arrange
        Map<String, String> modified = new HashMap<>(originalEnv);
        modified.put(ENV_NAME, "");
        setEnv(modified);
        // act
        Method hostMethod = BPGStatsdConfig.class.getDeclaredMethod("host");
        hostMethod.setAccessible(true);
        Object returned = hostMethod.invoke(new BPGStatsdConfig());
        // assert
        assertNotNull(returned);
        assertEquals("", returned);
    }

    // Helper to set environment variables for the running JVM (used for testing).
    // This uses reflection hacks and attempts multiple strategies for compatibility across JDK implementations.
    @SuppressWarnings("unchecked")
    private static void setEnv(Map<String, String> newenv) throws Exception {
        try {
            // For Oracle / OpenJDK
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.clear();
            env.putAll(newenv);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
            cienv.clear();
            cienv.putAll(newenv);
        } catch (NoSuchFieldException | ClassNotFoundException e) {
            // For other JDKs: modify the unmodifiable map's backing map
            Map<String, String> env = System.getenv();
            Class<?>[] classes = Collections.class.getDeclaredClasses();
            for (Class<?> cl : classes) {
                if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                    Field m = cl.getDeclaredField("m");
                    m.setAccessible(true);
                    Object obj = m.get(env);
                    Map<String, String> map = (Map<String, String>) obj;
                    map.clear();
                    map.putAll(newenv);
                }
            }
        }
    }
}
