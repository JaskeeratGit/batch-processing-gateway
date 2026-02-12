package com.apple.spark.util;

import com.apple.spark.util.ConfigUtil;
import com.apple.spark.AppConfig;
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
public class ConfigUtilGetSparkUIUrlTest {

    @Mock
    private AppConfig.SparkCluster cluster;

    @Test
    void testNamespaceNonNull() {
        when(cluster.getSparkUIUrl()).thenReturn("http://h");
        when(cluster.getSparkApplicationNamespace()).thenReturn("ns");
        String result = ConfigUtil.getSparkUIUrl(cluster, "sid");
        assertEquals("http://h/ns/sid", result);
    }

    @Test
    void testNullNamespaceOnly() {
        when(cluster.getSparkUIUrl()).thenReturn("http://h");
        when(cluster.getSparkApplicationNamespace()).thenReturn(null);
        String result = ConfigUtil.getSparkUIUrl(cluster, "sid");
        assertEquals("http://h/null/sid", result);
    }

    @Test
    void testNullSparkUIUrl() {
        when(cluster.getSparkUIUrl()).thenReturn(null);
        when(cluster.getSparkApplicationNamespace()).thenReturn("ns");
        String result = ConfigUtil.getSparkUIUrl(cluster, "sid");
        assertEquals("null/ns/sid", result);
    }

    @Test
    void testBothNull() {
        when(cluster.getSparkUIUrl()).thenReturn(null);
        when(cluster.getSparkApplicationNamespace()).thenReturn(null);
        String result = ConfigUtil.getSparkUIUrl(cluster, "sid");
        assertEquals("null/null/sid", result);
    }

    @Test
    void testNullSubmissionId() {
        when(cluster.getSparkUIUrl()).thenReturn("http://h");
        when(cluster.getSparkApplicationNamespace()).thenReturn("ns");
        String result = ConfigUtil.getSparkUIUrl(cluster, null);
        assertEquals("http://h/ns/null", result);
    }
}
