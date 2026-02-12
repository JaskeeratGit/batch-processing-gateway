package com.apple.spark.util;

import com.apple.spark.AppConfig;
import com.apple.spark.util.ConfigUtil;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ConfigUtil_getSparkUIUrl_1_0_Test_testNullUrlOnly {

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
