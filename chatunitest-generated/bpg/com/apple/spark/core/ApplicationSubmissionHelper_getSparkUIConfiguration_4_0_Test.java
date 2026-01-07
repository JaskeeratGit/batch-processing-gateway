package com.apple.spark.core;

import com.apple.spark.AppConfig;
import com.apple.spark.operator.SparkUIConfiguration;
import java.util.HashMap;
import java.util.Map;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static com.apple.spark.core.BatchSchedulerConstants.PLACEHOLDER_TIMEOUT_IN_SECONDS;
import static com.apple.spark.core.BatchSchedulerConstants.YUNIKORN_ROOT_QUEUE;
import static com.apple.spark.core.BatchSchedulerConstants.YUNIKORN_SPARK_DEFAULT_QUEUE;
import static com.apple.spark.core.Constants.*;
import static com.apple.spark.core.SparkConstants.CORE_LIMIT_RATIO;
import static com.apple.spark.core.SparkConstants.DRIVER_CPU_BUFFER_RATIO;
import static com.apple.spark.core.SparkConstants.DRIVER_MEM_BUFFER_RATIO;
import static com.apple.spark.core.SparkConstants.EXECUTOR_CPU_BUFFER_RATIO;
import static com.apple.spark.core.SparkConstants.EXECUTOR_MEM_BUFFER_RATIO;
import static com.apple.spark.core.SparkPodNodeAffinityHelper.createNodeAffinityForSparkPods;
import com.apple.spark.AppConfig.SparkCluster;
import com.apple.spark.api.SubmitApplicationRequest;
import com.apple.spark.operator.Affinity;
import com.apple.spark.operator.BatchSchedulerConfiguration;
import com.apple.spark.operator.DriverSpec;
import com.apple.spark.operator.ExecutorSpec;
import com.apple.spark.operator.NodeAffinity;
import com.apple.spark.operator.SparkApplicationSpec;
import com.apple.spark.operator.Volume;
import com.apple.spark.util.ExceptionUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import io.fabric8.kubernetes.api.model.PodDNSConfig;
import io.fabric8.kubernetes.api.model.PodDNSConfigOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationSubmissionHelper_getSparkUIConfiguration_4_0_Test {

    @Test
    public void testGetSparkUIConfiguration_whenSparkUIOptionsIsNull_returnsNull() {
        AppConfig.SparkCluster sparkCluster = new AppConfig.SparkCluster() {

            @Override
            public SparkUIConfiguration getSparkUIOptions() {
                return null;
            }
        };
        SparkUIConfiguration result = ApplicationSubmissionHelper.getSparkUIConfiguration("submission-1", sparkCluster);
        assertNull(result, "Expected null when sparkCluster.getSparkUIOptions() is null");
    }

    @Test
    public void testGetSparkUIConfiguration_whenIngressAnnotationsIsNull_copiesServicePortAndNoAnnotations() {
        SparkUIConfiguration original = new SparkUIConfiguration();
        original.setServicePort(8080);
        // keep ingressAnnotations null to test that branch
        original.setIngressAnnotations(null);
        AppConfig.SparkCluster sparkCluster = new AppConfig.SparkCluster() {

            @Override
            public SparkUIConfiguration getSparkUIOptions() {
                return original;
            }
        };
        SparkUIConfiguration result = ApplicationSubmissionHelper.getSparkUIConfiguration("sub-2", sparkCluster);
        assertNotNull(result, "Resulting SparkUIConfiguration should not be null");
        assertEquals(8080, result.getServicePort(), "Service port should be copied from original");
        assertNull(result.getIngressAnnotations(), "Ingress annotations should remain null when original is null");
    }

    @Test
    public void testGetSparkUIConfiguration_whenIngressAnnotationsPresent_replacesPlaceholderAndCopiesMap() {
        SparkUIConfiguration original = new SparkUIConfiguration();
        original.setServicePort(7077);
        Map<String, String> annotations = new HashMap<>();
        // Use the same placeholder constant that ApplicationSubmissionHelper expects
        String placeholder = com.apple.spark.core.Constants.SPARK_APPLICATION_RESOURCE_NAME_VAR;
        annotations.put("anno1", "prefix-" + placeholder + "-suffix");
        // value equals placeholder only
        annotations.put("anno2", placeholder);
        original.setIngressAnnotations(annotations);
        AppConfig.SparkCluster sparkCluster = new AppConfig.SparkCluster() {

            @Override
            public SparkUIConfiguration getSparkUIOptions() {
                return original;
            }
        };
        String submissionId = "my-submission-123";
        SparkUIConfiguration result = ApplicationSubmissionHelper.getSparkUIConfiguration(submissionId, sparkCluster);
        assertNotNull(result, "Resulting SparkUIConfiguration should not be null");
        assertEquals(7077, result.getServicePort(), "Service port should be copied");
        Map<String, String> resultAnnotations = result.getIngressAnnotations();
        assertNotNull(resultAnnotations, "Ingress annotations should be non-null after copy");
        // Values should have placeholder replaced with submissionId
        assertEquals("prefix-" + submissionId + "-suffix", resultAnnotations.get("anno1"), "Placeholder should be replaced inside the annotation value");
        assertEquals(submissionId, resultAnnotations.get("anno2"), "Annotation value that equals the placeholder should be replaced entirely");
        // Ensure we have a defensive copy (not the same reference as the original map)
        assertNotSame(annotations, resultAnnotations, "Ingress annotations map should be a copy, not the same instance");
        // Ensure original map remains unchanged
        assertEquals("prefix-" + placeholder + "-suffix", annotations.get("anno1"), "Original map must remain unchanged");
        assertEquals(placeholder, annotations.get("anno2"), "Original map must remain unchanged");
    }

    @Test
    public void testGetSparkUIConfiguration_whenIngressAnnotationsEmpty_returnsEmptyCopy() {
        SparkUIConfiguration original = new SparkUIConfiguration();
        Map<String, String> annotations = new HashMap<>();
        // empty but non-null
        original.setIngressAnnotations(annotations);
        AppConfig.SparkCluster sparkCluster = new AppConfig.SparkCluster() {

            @Override
            public SparkUIConfiguration getSparkUIOptions() {
                return original;
            }
        };
        SparkUIConfiguration result = ApplicationSubmissionHelper.getSparkUIConfiguration("sub-empty", sparkCluster);
        assertNotNull(result, "Result should not be null even if original had empty annotations");
        assertNotNull(result.getIngressAnnotations(), "Ingress annotations should be non-null (copied empty map)");
        assertTrue(result.getIngressAnnotations().isEmpty(), "Copied annotations map should be empty");
        assertNotSame(annotations, result.getIngressAnnotations(), "Copied map should be a different instance");
    }
}
