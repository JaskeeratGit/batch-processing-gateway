package com.apple.spark.util;

import com.apple.spark.AppConfig;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static com.apple.spark.AppConfig.SparkCluster;
import com.apple.spark.core.DBConnection;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit tests for ConfigUtil.getSparkUIUrl(SparkCluster, String)
 */
public class ConfigUtil_getSparkUIUrl_1_0_Test {

    private Method getSparkUIUrlMethod() throws NoSuchMethodException {
        Method m = ConfigUtil.class.getDeclaredMethod("getSparkUIUrl", AppConfig.SparkCluster.class, String.class);
        m.setAccessible(true);
        return m;
    }

    private AppConfig.SparkCluster createCluster(String sparkUIUrl, String sparkApplicationNamespace) throws Exception {
        Class<AppConfig.SparkCluster> cls = AppConfig.SparkCluster.class;
        AppConfig.SparkCluster cluster = cls.getDeclaredConstructor().newInstance();
        setProperty(cluster, "sparkUIUrl", sparkUIUrl);
        setProperty(cluster, "sparkApplicationNamespace", sparkApplicationNamespace);
        return cluster;
    }

    private void setProperty(Object target, String name, String value) throws Exception {
        Class<?> cls = target.getClass();
        // Try setter first: setSparkUIUrl / setSparkApplicationNamespace
        String setter = "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
        try {
            Method m = cls.getMethod(setter, String.class);
            m.invoke(target, value);
            return;
        } catch (NoSuchMethodException ignored) {
            // try field
        }
        try {
            Field f = cls.getDeclaredField(name);
            f.setAccessible(true);
            f.set(target, value);
            return;
        } catch (NoSuchFieldException ignored) {
            // try alternative field names (in case of different naming)
        }
        // Try with leading lower-case after "spark" split, e.g., SparkUIUrl -> sparkUIUrl handled above.
        // As a last resort, try to find any field of type String and set next unset field (not ideal but fallback).
        for (Field f : cls.getDeclaredFields()) {
            if (f.getType().equals(String.class)) {
                f.setAccessible(true);
                f.set(target, value);
                return;
            }
        }
        throw new NoSuchFieldException("No suitable field or setter found for " + name + " in " + cls.getName());
    }

    @Test
    void testNormalCase() throws Exception {
        Method m = getSparkUIUrlMethod();
        AppConfig.SparkCluster cluster = createCluster("http://host:8080", "namespace");
        String submissionId = "sub123";
        String result = (String) m.invoke(null, cluster, submissionId);
        assertEquals("http://host:8080/namespace/sub123", result);
    }

    @Test
    void testEmptyNamespaceProducesDoubleSlash() throws Exception {
        Method m = getSparkUIUrlMethod();
        AppConfig.SparkCluster cluster = createCluster("http://host:8080", "");
        String submissionId = "id";
        String result = (String) m.invoke(null, cluster, submissionId);
        assertEquals("http://host:8080//id", result);
    }

    @Test
    void testEmptySubmissionIdProducesTrailingSlash() throws Exception {
        Method m = getSparkUIUrlMethod();
        AppConfig.SparkCluster cluster = createCluster("http://host", "ns");
        String submissionId = "";
        String result = (String) m.invoke(null, cluster, submissionId);
        assertEquals("http://host/ns/", result);
    }

    @Test
    void testNullSubmissionIdBecomesStringNull() throws Exception {
        Method m = getSparkUIUrlMethod();
        AppConfig.SparkCluster cluster = createCluster("u", "n");
        String result = (String) m.invoke(null, cluster, null);
        assertEquals("u/n/null", result);
    }

    @Test
    void testNullClusterThrowsInvocationTargetWithNPECause() throws Exception {
        Method m = getSparkUIUrlMethod();
        InvocationTargetException ex = assertThrows(InvocationTargetException.class, () -> {
            m.invoke(null, (Object) null, "id");
        });
        assertTrue(ex.getCause() instanceof NullPointerException);
    }

    @Test
    void testNullClusterFieldsProduceNullLiterals() throws Exception {
        Method m = getSparkUIUrlMethod();
        AppConfig.SparkCluster cluster = createCluster(null, null);
        String result = (String) m.invoke(null, cluster, null);
        assertEquals("null/null/null", result);
    }

    @Test
    void testNullUrlOnly() throws Exception {
        Method m = getSparkUIUrlMethod();
        AppConfig.SparkCluster cluster = createCluster(null, "ns");
        String result = (String) m.invoke(null, cluster, "sid");
        assertEquals("null/ns/sid", result);
    }

    @Test
    void testNullNamespaceOnly() throws Exception {
        Method m = getSparkUIUrlMethod();
        AppConfig.SparkCluster cluster = createCluster("http://h", null);
        String result = (String) m.invoke(null, cluster, "sid");
        assertEquals("http://h/null/sid", result);
    }
}
