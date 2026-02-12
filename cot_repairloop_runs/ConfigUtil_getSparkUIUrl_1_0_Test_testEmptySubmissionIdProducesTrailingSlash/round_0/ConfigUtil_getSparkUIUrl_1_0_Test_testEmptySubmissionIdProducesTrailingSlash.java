package com.apple.spark.util;

import com.apple.spark.AppConfig;
import com.apple.spark.AppConfig.SparkCluster;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ConfigUtil.getSparkUIUrl(SparkCluster, String)
 */
public class ConfigUtil_getSparkUIUrl_1_0_Test_testEmptySubmissionIdProducesTrailingSlash {

    @Test
    void testEmptySubmissionIdProducesTrailingSlash() {
        SparkCluster cluster = mock(AppConfig.SparkCluster.class);
        when(cluster.getSparkUIUrl()).thenReturn("http://host");
        when(cluster.getSparkApplicationNamespace()).thenReturn("ns");

        String submissionId = "";
        String result = ConfigUtil.getSparkUIUrl(cluster, submissionId);

        assertEquals("http://host/ns/", result);
    }

    @Test
    void testNonEmptySubmissionIdProducesNoTrailingSlashBeyondId() {
        SparkCluster cluster = mock(AppConfig.SparkCluster.class);
        when(cluster.getSparkUIUrl()).thenReturn("http://host");
        when(cluster.getSparkApplicationNamespace()).thenReturn("ns");

        String submissionId = "submission-123";
        String result = ConfigUtil.getSparkUIUrl(cluster, submissionId);

        assertEquals("http://host/ns/submission-123", result);
    }

    @Test
    void testNullSubmissionIdProducesLiteralNullSegment() {
        SparkCluster cluster = mock(AppConfig.SparkCluster.class);
        when(cluster.getSparkUIUrl()).thenReturn("http://host");
        when(cluster.getSparkApplicationNamespace()).thenReturn("ns");

        String submissionId = null;
        String result = ConfigUtil.getSparkUIUrl(cluster, submissionId);

        // String.format will insert "null" for a null argument
        assertEquals("http://host/ns/null", result);
    }
}
