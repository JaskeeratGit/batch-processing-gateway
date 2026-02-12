package com.apple.spark.util;

import com.apple.spark.AppConfig;
import com.apple.spark.util.ConfigUtil;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ConfigUtil_getSparkUIUrl_1_0_Test_testNullUrlOnly {

    private Method getSparkUIUrlMethod() throws NoSuchMethodException {
        Method m = ConfigUtil.class.getDeclaredMethod("getSparkUIUrl", AppConfig.SparkCluster.class, String.class);
        m.setAccessible(true);
        return m;
    }

    @Test
    void testNullUrlOnly() throws Exception {
        Method m = getSparkUIUrlMethod();
        AppConfig.SparkCluster cluster = mock(AppConfig.SparkCluster.class);
        when(cluster.getSparkApplicationNamespace()).thenReturn("ns");
        // getSparkUIUrl() will return null by default for the mock
        String result = (String) m.invoke(null, cluster, "sid");
        assertEquals("null/ns/sid", result);
    }
}
