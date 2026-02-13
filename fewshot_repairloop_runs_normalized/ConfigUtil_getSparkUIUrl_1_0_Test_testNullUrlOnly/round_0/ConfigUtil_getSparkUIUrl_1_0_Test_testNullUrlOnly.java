package com.apple.spark.util;

import com.apple.spark.util.ConfigUtil;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.apple.spark.AppConfig;

/**
 * Unit tests for ConfigUtil.getSparkUIUrl(SparkCluster, String)
 */
public class ConfigUtil_getSparkUIUrl_1_0_Test_testNullUrlOnly {

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

    private Method getSparkUIUrlMethod() throws NoSuchMethodException {
        Method m = ConfigUtil.class.getDeclaredMethod("getSparkUIUrl", AppConfig.SparkCluster.class, String.class);
        m.setAccessible(true);
        return m;
    }

    @Test
    void testNullUrlOnly() throws Exception {
        Method m = getSparkUIUrlMethod();
        AppConfig.SparkCluster cluster = new AppConfig.SparkCluster(null, "ns");
        String result = (String) m.invoke(null, cluster, "sid");
        assertEquals("null/ns/sid", result);
    }
}
