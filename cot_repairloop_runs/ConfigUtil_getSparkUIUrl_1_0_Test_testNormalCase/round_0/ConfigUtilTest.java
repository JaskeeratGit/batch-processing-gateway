package com.apple.spark.util;

import com.apple.spark.AppConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ConfigUtil.getSparkUIUrl(SparkCluster, String)
 */
@ExtendWith(MockitoExtension.class)
public class ConfigUtilTest {

    @Mock
    private AppConfig.SparkCluster cluster;

    @Test
    void testNormalCase() {
        when(cluster.getSparkUIUrl()).thenReturn("http://host:8080");
        when(cluster.getSparkApplicationNamespace()).thenReturn("namespace");

        String submissionId = "sub123";
        String result = ConfigUtil.getSparkUIUrl(cluster, submissionId);

        assertEquals("http://host:8080/namespace/sub123", result);
    }

    @Test
    void testEmptyNamespaceProducesDoubleSlash() {
        when(cluster.getSparkUIUrl()).thenReturn("http://host:8080");
        when(cluster.getSparkApplicationNamespace()).thenReturn("");

        String submissionId = "sub123";
        String result = ConfigUtil.getSparkUIUrl(cluster, submissionId);

        // Because the implementation simply concatenates with '/', an empty namespace yields a double slash
        assertEquals("http://host:8080//sub123", result);
    }

    @Test
    void testSubmissionIdWithInternalSlashPreserved() {
        when(cluster.getSparkUIUrl()).thenReturn("http://host:8080");
        when(cluster.getSparkApplicationNamespace()).thenReturn("namespace");

        String submissionId = "sub/123";
        String result = ConfigUtil.getSparkUIUrl(cluster, submissionId);

        // The method does not sanitize slashes inside submissionId
        assertEquals("http://host:8080/namespace/sub/123", result);
    }

    @Test
    void testNullReturnedPartsBecomeStringNulls() {
        when(cluster.getSparkUIUrl()).thenReturn(null);
        when(cluster.getSparkApplicationNamespace()).thenReturn(null);

        String submissionId = "sub123";
        String result = ConfigUtil.getSparkUIUrl(cluster, submissionId);

        // String.format will convert null arguments to the string "null"
        assertEquals("null/null/sub123", result);
    }

    @Test
    void testNullClusterThrowsNPE() {
        // If the cluster object itself is null, the method should throw a NullPointerException
        assertThrows(NullPointerException.class, () -> ConfigUtil.getSparkUIUrl(null, "sub123"));
    }
}
