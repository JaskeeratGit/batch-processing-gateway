package com.apple.spark.util;

import com.apple.spark.AppConfig;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ConfigUtil.getSparkUIUrl(SparkCluster, String)
 */
public class ConfigUtil_getSparkUIUrl_1_0_Test_testNullNamespaceOnly {

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
