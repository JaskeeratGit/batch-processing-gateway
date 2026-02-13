package com.apple.spark.util;

import com.apple.spark.AppConfig;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

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
    void testNullClusterThrowsInvocationTargetWithNPECause() throws Exception {
        Method m = getSparkUIUrlMethod();
        InvocationTargetException ex = assertThrows(InvocationTargetException.class, () -> {
            m.invoke(null, null, "id");
        });
        assertTrue(ex.getCause() instanceof NullPointerException);
    }
}
