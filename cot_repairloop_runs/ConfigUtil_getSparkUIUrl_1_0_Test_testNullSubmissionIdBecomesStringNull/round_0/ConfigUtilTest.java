package com.apple.spark.util;

import com.apple.spark.AppConfig;
import com.apple.spark.util.ConfigUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ConfigUtil.getSparkUIUrl(SparkCluster, String)
 */
public class ConfigUtilTest {

    @Test
    void testNullSubmissionIdBecomesStringNull() {
        // Create a Mockito mock of the SparkCluster nested class
        AppConfig.SparkCluster cluster = mock(AppConfig.SparkCluster.class);
        when(cluster.getSparkUIUrl()).thenReturn("u");
        when(cluster.getSparkApplicationNamespace()).thenReturn("n");

        String result = ConfigUtil.getSparkUIUrl(cluster, null);
        assertEquals("u/n/null", result);
    }

    @Test
    void testNonNullSubmissionId() {
        AppConfig.SparkCluster cluster = mock(AppConfig.SparkCluster.class);
        when(cluster.getSparkUIUrl()).thenReturn("http://example.com");
        when(cluster.getSparkApplicationNamespace()).thenReturn("namespace");

        String result = ConfigUtil.getSparkUIUrl(cluster, "submission-123");
        assertEquals("http://example.com/namespace/submission-123", result);
    }

    @Test
    void testEmptyNamespaceAndUrl() {
        AppConfig.SparkCluster cluster = mock(AppConfig.SparkCluster.class);
        when(cluster.getSparkUIUrl()).thenReturn("");
        when(cluster.getSparkApplicationNamespace()).thenReturn("");

        String result = ConfigUtil.getSparkUIUrl(cluster, "id");
        assertEquals("//id", result); // "" + "/" + "" + "/" + "id" => "/ / id" -> "//id"
    }
}
