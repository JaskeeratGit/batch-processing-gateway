package com.apple.spark.core;

import com.apple.spark.AppConfig;
import com.apple.spark.AppConfig.SparkCluster;
import com.apple.spark.api.SubmitApplicationRequest;
import com.apple.spark.operator.Affinity;
import com.apple.spark.operator.DriverSpec;
import com.apple.spark.operator.Volume;
import com.apple.spark.operator.VolumeMount;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
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
import com.apple.spark.operator.BatchSchedulerConfiguration;
import com.apple.spark.operator.ExecutorSpec;
import com.apple.spark.operator.NodeAffinity;
import com.apple.spark.operator.SparkApplicationSpec;
import com.apple.spark.operator.SparkUIConfiguration;
import com.apple.spark.util.ExceptionUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import io.fabric8.kubernetes.api.model.PodDNSConfig;
import io.fabric8.kubernetes.api.model.PodDNSConfigOption;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit tests for ApplicationSubmissionHelper.getDriverSpec(...)
 *
 * These tests aim to cover key branches:
 * - volume mounts selection from request vs cluster
 * - service account defaulting from cluster when driver SA empty
 * - adjustment of cores and coreLimits (both when coreLimit is null and when provided)
 * - memory adjustment
 * - skipping node affinity creation when affinity already present
 * - verifying private mem parsing helpers via reflection
 */
@ExtendWith(MockitoExtension.class)
public class ApplicationSubmissionHelper_getDriverSpec_14_0_Test_test_getDriverSpec_requestVolumesPriority_and_coreLimitNull_and_serviceAccountDefaulting {

    @Mock
    private SparkCluster mockSparkCluster;

    @Test
    public void test_getDriverSpec_requestVolumesPriority_and_coreLimitNull_and_serviceAccountDefaulting() throws Exception {
        // Arrange
        SubmitApplicationRequest req = new SubmitApplicationRequest();
        DriverSpec reqDriver = new DriverSpec();
        // set initial cores and memory
        reqDriver.setCores(3);
        // force branch where coreLimit is computed
        reqDriver.setCoreLimit(null);
        reqDriver.setMemory("256m");
        // leave serviceAccount null to test defaulting from sparkCluster
        reqDriver.setServiceAccount(null);
        // create a volumes list on the request (so request volumes take priority)
        List<Volume> reqVolumes = Collections.singletonList(new Volume());
        req.setVolumes(reqVolumes);
        req.setDriver(reqDriver);
        // AppConfig with no queue overrides so default buffer ratios are used
        AppConfig appConfig = new AppConfig();
        appConfig.setQueues(Collections.emptyList());
        // Spark cluster that also has a driver (should not be used for volumeMounts since request volumes exist)
        DriverSpec clusterDriver = new DriverSpec();
        List<VolumeMount> clusterMounts = new ArrayList<>();
        clusterDriver.setVolumeMounts(clusterMounts);
        when(mockSparkCluster.getDriver()).thenReturn(clusterDriver);
        // cluster volumes present but request volumes should take precedence
        when(mockSparkCluster.getVolumes()).thenReturn(Collections.singletonList(new Volume()));
        when(mockSparkCluster.getSparkServiceAccount()).thenReturn("cluster-sa");
        // Act
        DriverSpec result = ApplicationSubmissionHelper.getDriverSpec(req, appConfig, "any-queue", mockSparkCluster);
        // Assert core adjustment: cores rounded from original * DRIVER_CPU_BUFFER_RATIO
        double cpuBuffer = com.apple.spark.core.SparkConstants.DRIVER_CPU_BUFFER_RATIO;
        int expectedAdjustedCores = (int) Math.round(3 * cpuBuffer);
        assertEquals(expectedAdjustedCores, result.getCores(), "Driver cores should be adjusted by CPU buffer ratio");
        // Assert coreLimit computed (should end with 'm' as method formats "%sm")
        double coreLimitRatio = com.apple.spark.core.SparkConstants.CORE_LIMIT_RATIO;
        // The code uses Math.ceil(driverSpec.getCores() * CORE_LIMIT_RATIO * driverCpuBufferRatioQueue)
        long expectedCoreLimit = (long) Math.ceil(expectedAdjustedCores * coreLimitRatio * cpuBuffer);
        assertNotNull(result.getCoreLimit());
        assertTrue(result.getCoreLimit().endsWith("m"));
        assertEquals(String.format("%sm", expectedCoreLimit), result.getCoreLimit());
        // Assert memory adjusted
        double memBuffer = com.apple.spark.core.SparkConstants.DRIVER_MEM_BUFFER_RATIO;
        long expectedMemNumber = (long) Math.ceil(256L * memBuffer);
        // getMemUnitFromRequestStr should return "m"
        assertEquals(String.valueOf(expectedMemNumber) + "m", result.getMemory());
        // service account should be set from cluster since driver had none
        assertEquals("cluster-sa", result.getServiceAccount());
        // volume mounts: since request specified volumes, driver volume mounts should come from request.getDriver().getVolumeMounts()
        // request driver had no explicit volumeMounts set, so result.getVolumeMounts() should be whatever request driver's (null)
        // but importantly the code prefers request.getDriver().getVolumeMounts() when request.getVolumes() is non-empty.
        // Since reqDriver had no mounts, expect null or empty (we assert not to be the clusterMounts)
        assertNotEquals(clusterMounts, result.getVolumeMounts());
    }


}
