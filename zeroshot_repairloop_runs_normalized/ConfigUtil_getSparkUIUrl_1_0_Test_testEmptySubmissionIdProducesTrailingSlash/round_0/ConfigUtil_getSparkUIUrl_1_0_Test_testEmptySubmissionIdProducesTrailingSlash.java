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

    // The nested SparkCluster here is not used by the test; it's left out to avoid conflicts.
}

class ConfigUtilTest_Invoker {

    private Method getSparkUIUrlMethod() throws NoSuchMethodException {
        Method m = ConfigUtil.class.getDeclaredMethod("getSparkUIUrl", AppConfig.SparkCluster.class, String.class);
        m.setAccessible(true);
        return m;
    }

    @Test
    void testEmptySubmissionIdProducesTrailingSlash() throws Exception {
        Method m = getSparkUIUrlMethod();
        AppConfig.SparkCluster cluster = new AppConfig.SparkCluster("http://host", "ns");
        String submissionId = "";
        String result = (String) m.invoke(null, cluster, submissionId);
        assertEquals("http://host/ns/", result);
    }
}
