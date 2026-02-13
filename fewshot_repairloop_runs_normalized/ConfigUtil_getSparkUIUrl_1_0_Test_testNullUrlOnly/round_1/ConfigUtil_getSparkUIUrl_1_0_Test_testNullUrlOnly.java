package com.apple.spark.util;

import com.apple.spark.util.ConfigUtil;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.apple.spark.AppConfig;
import org.mockito.Mockito;

/**
 * Unit tests for ConfigUtil.getSparkUIUrl(SparkCluster, String)
 */
public class ConfigUtil_getSparkUIUrl_1_0_Test_testNullUrlOnly {

    private Method getSparkUIUrlMethod() throws NoSuchMethodException {
        Method m = ConfigUtil.class.getDeclaredMethod("getSparkUIUrl", AppConfig.SparkCluster.class, String.class);
        m.setAccessible(true);
        return m;
    }

    @Test
    void testNullUrlOnly() throws Exception {
        Method m = getSparkUIUrlMethod();
        AppConfig.SparkCluster cluster = Mockito.mock(AppConfig.SparkCluster.class);
        Mockito.when(cluster.getSparkUIUrl()).thenReturn(null);
        Mockito.when(cluster.getSparkApplicationNamespace()).thenReturn("ns");
        String result = (String) m.invoke(null, cluster, "sid");
        assertEquals("null/ns/sid", result);
    }
}
