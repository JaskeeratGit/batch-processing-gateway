package com.apple.spark.util;

import com.apple.spark.util.ConfigUtil;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static com.apple.spark.AppConfig.SparkCluster;
import com.apple.spark.AppConfig;

/**
 * Unit tests for ConfigUtil.getSparkUIUrl(SparkCluster, String)
 */
public class ConfigUtil_getSparkUIUrl_1_0_Test_testEmptyNamespaceProducesDoubleSlash {

    private Method getSparkUIUrlMethod() throws NoSuchMethodException {
        Method m = ConfigUtil.class.getDeclaredMethod("getSparkUIUrl", AppConfig.SparkCluster.class, String.class);
        m.setAccessible(true);
        return m;
    }

    @Test
    void testEmptyNamespaceProducesDoubleSlash() throws Exception {
        Method m = getSparkUIUrlMethod();
        AppConfig.SparkCluster cluster = new AppConfig.SparkCluster("http://host:8080", "");
        String submissionId = "id";
        String result = (String) m.invoke(null, cluster, submissionId);
        assertEquals("http://host:8080//id", result);
    }
}
