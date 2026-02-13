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
class ConfigUtilTest {

    private Method getSparkUIUrlMethod() throws NoSuchMethodException {
        Method m = ConfigUtil.class.getDeclaredMethod("getSparkUIUrl", AppConfig.SparkCluster.class, String.class);
        m.setAccessible(true);
        return m;
    }

    @Test
    void testNullNamespaceOnly() throws Exception {
        Method m = getSparkUIUrlMethod();
        AppConfig.SparkCluster cluster = mock(AppConfig.SparkCluster.class);
        when(cluster.getSparkUIUrl()).thenReturn("http://h");
        when(cluster.getSparkApplicationNamespace()).thenReturn(null);
        String result = (String) m.invoke(null, cluster, "sid");
        assertEquals("http://h/null/sid", result);
    }
}
