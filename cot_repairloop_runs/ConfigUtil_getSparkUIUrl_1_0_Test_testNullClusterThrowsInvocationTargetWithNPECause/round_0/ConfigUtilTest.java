package com.apple.spark.util;

import com.apple.spark.AppConfig.SparkCluster;
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
    private SparkCluster cluster;

    @Test
    void testNullClusterThrowsNPE() {
        // Direct call to the method under test with a null cluster should throw NPE
        assertThrows(NullPointerException.class, () -> ConfigUtil.getSparkUIUrl(null, "id"));
    }

    @Test
    void testGetSparkUIUrlFormatsProperly() {
        when(cluster.getSparkUIUrl()).thenReturn("http://example.com");
        when(cluster.getSparkApplicationNamespace()).thenReturn("namespace");

        String result = ConfigUtil.getSparkUIUrl(cluster, "submission-123");
        assertEquals("http://example.com/namespace/submission-123", result);
    }

    @Test
    void testNullPartsAreRenderedAsStringNull() {
        // If the cluster provides null values (or submissionId is null), String.format prints "null" for those parts.
        when(cluster.getSparkUIUrl()).thenReturn(null);
        when(cluster.getSparkApplicationNamespace()).thenReturn(null);

        String result = ConfigUtil.getSparkUIUrl(cluster, null);
        assertEquals("null/null/null", result);
    }
}
