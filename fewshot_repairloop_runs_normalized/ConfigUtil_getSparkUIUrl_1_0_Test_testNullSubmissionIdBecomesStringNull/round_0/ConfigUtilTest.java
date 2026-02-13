package com.apple.spark.util;

import com.apple.spark.AppConfig;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ConfigUtilTest {

    private Method getSparkUIUrlMethod() throws NoSuchMethodException {
        Method m = ConfigUtil.class.getDeclaredMethod("getSparkUIUrl", AppConfig.SparkCluster.class, String.class);
        m.setAccessible(true);
        return m;
    }

    @Test
    void testNullSubmissionIdBecomesStringNull() throws Exception {
        Method m = getSparkUIUrlMethod();
        AppConfig.SparkCluster cluster = mock(AppConfig.SparkCluster.class);
        when(cluster.getSparkUIUrl()).thenReturn("u");
        when(cluster.getSparkApplicationNamespace()).thenReturn("n");
        String result = (String) m.invoke(null, cluster, null);
        assertEquals("u/n/null", result);
    }
}
