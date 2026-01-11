package com.apple.spark.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import com.apple.spark.AppConfig;
import com.apple.spark.AppConfig.SparkCluster;
import com.apple.spark.api.SubmitApplicationRequest;
import com.apple.spark.operator.Affinity;
import com.apple.spark.operator.BatchSchedulerConfiguration;
import com.apple.spark.operator.DriverSpec;
import com.apple.spark.operator.ExecutorSpec;
import com.apple.spark.operator.NodeAffinity;
import com.apple.spark.operator.SparkApplicationSpec;
import com.apple.spark.operator.SparkUIConfiguration;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationSubmissionHelper_getClusterIdFromSubmissionId_9_0_Test {

    @Test
    public void testGetClusterIdFromSubmissionId_validSimple_returnsClusterId() {
        String submissionId = "c01-abc123";
        String clusterId = ApplicationSubmissionHelper.getClusterIdFromSubmissionId(submissionId);
        assertEquals("c01", clusterId);
    }

    @Test
    public void testGetClusterIdFromSubmissionId_validMultipleDashes_returnsPrefixBeforeFirstDash() {
        String submissionId = "cluster-123-456";
        String clusterId = ApplicationSubmissionHelper.getClusterIdFromSubmissionId(submissionId);
        assertEquals("cluster", clusterId);
    }

    @Test
    public void testGetClusterIdFromSubmissionId_dashAtEnd_returnsPrefix() {
        String submissionId = "c01-";
        String clusterId = ApplicationSubmissionHelper.getClusterIdFromSubmissionId(submissionId);
        assertEquals("c01", clusterId);
    }

    @Test
    public void testGetClusterIdFromSubmissionId_null_throwsInvalidSubmissionIdException() {
        assertThrows(InvalidSubmissionIdException.class, () -> ApplicationSubmissionHelper.getClusterIdFromSubmissionId(null));
    }

    @Test
    public void testGetClusterIdFromSubmissionId_noDash_throwsInvalidSubmissionIdException() {
        assertThrows(InvalidSubmissionIdException.class, () -> ApplicationSubmissionHelper.getClusterIdFromSubmissionId("noDashHere"));
    }

    @Test
    public void testGetClusterIdFromSubmissionId_startsWithDash_throwsInvalidSubmissionIdException() throws Exception {
        // Use reflection to invoke the method and capture the InvocationTargetException wrapper
        Method m = ApplicationSubmissionHelper.class.getDeclaredMethod("getClusterIdFromSubmissionId", String.class);
        m.setAccessible(true);
        InvocationTargetException ite = assertThrows(InvocationTargetException.class, () -> {
            // invocation wraps the thrown InvalidSubmissionIdException inside InvocationTargetException
            m.invoke(null, "-startsWithDash");
        });
        Throwable cause = ite.getCause();
        assertTrue(cause instanceof InvalidSubmissionIdException, "Cause should be InvalidSubmissionIdException");
    }
}
