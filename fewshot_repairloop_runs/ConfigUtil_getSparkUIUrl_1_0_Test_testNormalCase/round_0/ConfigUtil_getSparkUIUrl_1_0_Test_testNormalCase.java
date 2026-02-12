package com.apple.spark.util;

import com.apple.spark.util.ConfigUtil;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import com.apple.spark.AppConfig;

/**
 * Unit tests for ConfigUtil.getSparkUIUrl(SparkCluster, String)
 */
public class ConfigUtil_getSparkUIUrl_1_0_Test_testNormalCase {

    @Test
    void testNormalCase() throws Exception {
        AppConfig.SparkCluster cluster = mock(AppConfig.SparkCluster.class);
        when(cluster.getSparkUIUrl()).thenReturn("http://host:8080");
        when(cluster.getSparkApplicationNamespace()).thenReturn("namespace");

        String submissionId = "sub123";
        String result = ConfigUtil.getSparkUIUrl(cluster, submissionId);
        assertEquals("http://host:8080/namespace/sub123", result);
    }
}
