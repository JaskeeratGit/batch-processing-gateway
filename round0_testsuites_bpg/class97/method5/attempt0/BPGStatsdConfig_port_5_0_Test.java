package com.apple.spark.core;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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

public class BPGStatsdConfig_port_5_0_Test {

    private static final String STATSD_SERVER_PORT_ENV_NAME = "STATSD_SERVER_PORT";

    private Map<String, String> originalEnv;

    @BeforeEach
    public void setUp() throws Exception {
        // snapshot original environment
        originalEnv = new HashMap<>(System.getenv());
    }

    @AfterEach
    public void tearDown() throws Exception {
        // restore original environment
        setEnvMap(originalEnv);
    }

    @Test
    public void testPort_whenEnvNotSet_returnsZero() throws Exception {
        // remove env var if present
        setEnvVar(STATSD_SERVER_PORT_ENV_NAME, null);
        int result = invokePortViaReflection();
        assertEquals(0, result);
    }

    @Test
    public void testPort_whenEnvEmpty_returnsZero() throws Exception {
        setEnvVar(STATSD_SERVER_PORT_ENV_NAME, "");
        int result = invokePortViaReflection();
        assertEquals(0, result);
    }

    @Test
    public void testPort_whenEnvIsValidNumber_returnsParsedInt() throws Exception {
        setEnvVar(STATSD_SERVER_PORT_ENV_NAME, "8125");
        int result = invokePortViaReflection();
        assertEquals(8125, result);
    }

    @Test
    public void testPort_whenEnvIsInvalidNumber_returnsZero() throws Exception {
        setEnvVar(STATSD_SERVER_PORT_ENV_NAME, "notANumber");
        int result = invokePortViaReflection();
        assertEquals(0, result);
    }

    // Helper to invoke port() via reflection (as requested)
    private int invokePortViaReflection() throws Exception {
        Class<?> clazz = Class.forName("com.apple.spark.core.BPGStatsdConfig");
        Object instance = clazz.getDeclaredConstructor().newInstance();
        Method m = clazz.getDeclaredMethod("port");
        m.setAccessible(true);
        Object ret = m.invoke(instance);
        return (ret instanceof Integer) ? (Integer) ret : 0;
    }

    // Helpers to modify the environment map via reflection
    private void setEnvVar(String key, String value) throws Exception {
        Map<String, String> toPut = new HashMap<>();
        Map<String, String> current = System.getenv();
        if (value != null) {
            toPut.put(key, value);
        }
        setEnvWithModifications(toPut, value == null ? new String[] { key } : new String[] {});
    }

    @SuppressWarnings("unchecked")
    private void setEnvMap(Map<String, String> newenv) throws Exception {
        // Attempt to set the underlying environment map to newenv
        Map<String, String> env = System.getenv();
        Class<?> envClass = env.getClass();
        try {
            Field m = envClass.getDeclaredField("m");
            m.setAccessible(true);
            Object obj = m.get(env);
            if (obj instanceof Map) {
                Map<String, String> modifiable = (Map<String, String>) obj;
                modifiable.clear();
                modifiable.putAll(newenv);
                return;
            }
        } catch (NoSuchFieldException ignored) {
            // continue to other strategies
        }
        // For Java 8 and some other builds: modify ProcessEnvironment.theEnvironment and theCaseInsensitiveEnvironment
        try {
            Class<?> pe = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvField = pe.getDeclaredField("theEnvironment");
            theEnvField.setAccessible(true);
            Map<String, String> envMap = (Map<String, String>) theEnvField.get(null);
            envMap.clear();
            envMap.putAll(newenv);
            try {
                Field ciField = pe.getDeclaredField("theCaseInsensitiveEnvironment");
                ciField.setAccessible(true);
                Map<String, String> ciMap = (Map<String, String>) ciField.get(null);
                ciMap.clear();
                ciMap.putAll(newenv);
            } catch (NoSuchFieldException ignored) {
                // ignore if not present
            }
            return;
        } catch (ClassNotFoundException | NoSuchFieldException ignored) {
            // fallback below
        }
        // Fallback: manipulate the private 'm' of Collections$UnmodifiableMap returned by System.getenv()
        Field[] fields = envClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().equals("m")) {
                field.setAccessible(true);
                Object obj = field.get(env);
                if (obj instanceof Map) {
                    Map<String, String> modifiable = (Map<String, String>) obj;
                    modifiable.clear();
                    modifiable.putAll(newenv);
                    return;
                }
            }
        }
        throw new IllegalStateException("Unable to set environment variables for tests on this JVM");
    }

    @SuppressWarnings("unchecked")
    private void setEnvWithModifications(Map<String, String> toAdd, String[] toRemove) throws Exception {
        Map<String, String> env = System.getenv();
        Class<?> envClass = env.getClass();
        // Try direct access via 'm' (Collections$UnmodifiableMap)
        try {
            Field m = envClass.getDeclaredField("m");
            m.setAccessible(true);
            Object obj = m.get(env);
            if (obj instanceof Map) {
                Map<String, String> modifiable = (Map<String, String>) obj;
                if (toAdd != null)
                    modifiable.putAll(toAdd);
                if (toRemove != null) {
                    for (String k : toRemove) modifiable.remove(k);
                }
                return;
            }
        } catch (NoSuchFieldException ignored) {
            // fallback
        }
        // Try ProcessEnvironment
        try {
            Class<?> pe = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvField = pe.getDeclaredField("theEnvironment");
            theEnvField.setAccessible(true);
            Map<String, String> envMap = (Map<String, String>) theEnvField.get(null);
            if (toAdd != null)
                envMap.putAll(toAdd);
            if (toRemove != null) {
                for (String k : toRemove) envMap.remove(k);
            }
            try {
                Field ciField = pe.getDeclaredField("theCaseInsensitiveEnvironment");
                ciField.setAccessible(true);
                Map<String, String> ciMap = (Map<String, String>) ciField.get(null);
                if (toAdd != null)
                    ciMap.putAll(toAdd);
                if (toRemove != null) {
                    for (String k : toRemove) ciMap.remove(k);
                }
            } catch (NoSuchFieldException ignored) {
                // ignore
            }
            return;
        } catch (ClassNotFoundException | NoSuchFieldException ignored) {
            // fallback
        }
        // Final fallback: try to find and modify the wrapped map field 'm' reflectively
        Field[] fields = envClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().equals("m")) {
                field.setAccessible(true);
                Object obj = field.get(env);
                if (obj instanceof Map) {
                    Map<String, String> modifiable = (Map<String, String>) obj;
                    if (toAdd != null)
                        modifiable.putAll(toAdd);
                    if (toRemove != null) {
                        for (String k : toRemove) modifiable.remove(k);
                    }
                    return;
                }
            }
        }
        throw new IllegalStateException("Unable to modify environment variables for tests on this JVM");
    }
}
