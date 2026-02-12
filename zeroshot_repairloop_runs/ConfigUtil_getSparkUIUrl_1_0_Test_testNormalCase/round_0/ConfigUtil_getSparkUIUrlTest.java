package com.apple.spark.util;

import com.apple.spark.AppConfig;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for ConfigUtil.getSparkUIUrl(SparkCluster, String)
 */
@ExtendWith(MockitoExtension.class)
public class ConfigUtil_getSparkUIUrlTest {

    @Mock
    private AppConfig.SparkCluster clusterMock;

    private Method getSparkUIUrlMethod() throws NoSuchMethodException {
        Method m = ConfigUtil.class.getDeclaredMethod("getSparkUIUrl", AppConfig.SparkCluster.class, String.class);
        m.setAccessible(true);
        return m;
    }

    @Test
    void testNormalCase() throws Exception {
        when(clusterMock.getSparkUIUrl()).thenReturn("http://host:8080");
        when(clusterMock.getSparkApplicationNamespace()).thenReturn("namespace");

        Method m = getSparkUIUrlMethod();
        String result = (String) m.invoke(null, clusterMock, "sub123");
        assertEquals("http://host:8080/namespace/sub123", result);
    }
}
