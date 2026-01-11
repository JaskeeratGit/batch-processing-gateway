package com.apple.spark.util;

import com.apple.spark.util.ConfigUtil;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static com.apple.spark.AppConfig.SparkCluster;
import com.apple.spark.AppConfig;
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

    public static class SparkCluster {

        private final String sparkUIUrl;

        private final String sparkApplicationNamespace;

        public SparkCluster(String sparkUIUrl, String sparkApplicationNamespace) {
            this.sparkUIUrl = sparkUIUrl;
            this.sparkApplicationNamespace = sparkApplicationNamespace;
        }

        public String getSparkUIUrl() {
            return sparkUIUrl;
        }

        public String getSparkApplicationNamespace() {
            return sparkApplicationNamespace;
        }
    }
}

class ConfigUtilTest {

    private Method getSparkUIUrlMethod() throws NoSuchMethodException {
        Method m = ConfigUtil.class.getDeclaredMethod("getSparkUIUrl", AppConfig.SparkCluster.class, String.class);
        m.setAccessible(true);
        return m;
    }

    @Test
    void testNormalCase() throws Exception {
        Method m = getSparkUIUrlMethod();
        AppConfig.SparkCluster cluster = new AppConfig.SparkCluster("http://host:8080", "namespace");
        String submissionId = "sub123";
        String result = (String) m.invoke(null, cluster, submissionId);
        assertEquals("http://host:8080/namespace/sub123", result);
    }

    @Test
    void testEmptyNamespaceProducesDoubleSlash() throws Exception {
        Method m = getSparkUIUrlMethod();
        AppConfig.SparkCluster cluster = new AppConfig.SparkCluster("http://host:8080", "");
        String submissionId = "id";
        String result = (String) m.invoke(null, cluster, submissionId);
        assertEquals("http://host:8080//id", result);
    }

    @Test
    void testEmptySubmissionIdProducesTrailingSlash() throws Exception {
        Method m = getSparkUIUrlMethod();
        AppConfig.SparkCluster cluster = new AppConfig.SparkCluster("http://host", "ns");
        String submissionId = "";
        String result = (String) m.invoke(null, cluster, submissionId);
        assertEquals("http://host/ns/", result);
    }

    @Test
    void testNullSubmissionIdBecomesStringNull() throws Exception {
        Method m = getSparkUIUrlMethod();
        AppConfig.SparkCluster cluster = new AppConfig.SparkCluster("u", "n");
        String result = (String) m.invoke(null, cluster, null);
        assertEquals("u/n/null", result);
    }

    @Test
    void testNullClusterThrowsInvocationTargetWithNPECause() throws Exception {
        Method m = getSparkUIUrlMethod();
        InvocationTargetException ex = assertThrows(InvocationTargetException.class, () -> {
            m.invoke(null, null, "id");
        });
        assertTrue(ex.getCause() instanceof NullPointerException);
    }

    @Test
    void testNullClusterFieldsProduceNullLiterals() throws Exception {
        Method m = getSparkUIUrlMethod();
        AppConfig.SparkCluster cluster = new AppConfig.SparkCluster(null, null);
        String result = (String) m.invoke(null, cluster, null);
        assertEquals("null/null/null", result);
    }

    @Test
    void testNullUrlOnly() throws Exception {
        Method m = getSparkUIUrlMethod();
        AppConfig.SparkCluster cluster = new AppConfig.SparkCluster(null, "ns");
        String result = (String) m.invoke(null, cluster, "sid");
        assertEquals("null/ns/sid", result);
    }

    @Test
    void testNullNamespaceOnly() throws Exception {
        Method m = getSparkUIUrlMethod();
        AppConfig.SparkCluster cluster = new AppConfig.SparkCluster("http://h", null);
        String result = (String) m.invoke(null, cluster, "sid");
        assertEquals("http://h/null/sid", result);
    }
}
