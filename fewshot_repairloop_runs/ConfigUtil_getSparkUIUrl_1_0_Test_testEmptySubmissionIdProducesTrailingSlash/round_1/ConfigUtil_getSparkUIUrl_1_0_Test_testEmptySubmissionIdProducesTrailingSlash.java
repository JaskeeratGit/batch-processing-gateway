package com.apple.spark.util;

import com.apple.spark.util.ConfigUtil;
import java.lang.reflect.Method;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import com.apple.spark.AppConfig;

/**
 * Unit tests for ConfigUtil.getSparkUIUrl(SparkCluster, String)
 */
public class ConfigUtil_getSparkUIUrl_1_0_Test_testEmptySubmissionIdProducesTrailingSlash {

    private Method getSparkUIUrlMethod() throws NoSuchMethodException {
        Method m = ConfigUtil.class.getDeclaredMethod("getSparkUIUrl", AppConfig.SparkCluster.class, String.class);
        m.setAccessible(true);
        return m;
    }

    @Test
    void testEmptySubmissionIdProducesTrailingSlash() throws Exception {
        Method m = getSparkUIUrlMethod();
        AppConfig.SparkCluster cluster = new AppConfig.SparkCluster() {
            @Override
            public String getSparkUIUrl() {
                return "http://host";
            }

            @Override
            public String getSparkApplicationNamespace() {
                return "ns";
            }
        };
        String submissionId = "";
        String result = (String) m.invoke(null, cluster, submissionId);
        assertEquals("http://host/ns/", result);
    }
}
