package com.apple.spark.util;

import com.apple.spark.AppConfig;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ConfigUtil_getSparkUIUrl_EmptyNamespace_Test {

    private Method getSparkUIUrlMethod() throws NoSuchMethodException {
        Method m = ConfigUtil.class.getDeclaredMethod("getSparkUIUrl", AppConfig.SparkCluster.class, String.class);
        m.setAccessible(true);
        return m;
    }

    @Test
    void testEmptyNamespaceProducesDoubleSlash() throws Exception {
        Method m = getSparkUIUrlMethod();
        AppConfig.SparkCluster cluster = mock(AppConfig.SparkCluster.class);
        when(cluster.getSparkUIUrl()).thenReturn("http://host:8080");
        when(cluster.getSparkApplicationNamespace()).thenReturn("");
        String submissionId = "id";
        String result = (String) m.invoke(null, cluster, submissionId);
        assertEquals("http://host:8080//id", result);
    }
}
