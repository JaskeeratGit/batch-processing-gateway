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
public class ApplicationSubmissionHelper_getDriverSpec_14_0_Test_test_getDriverSpec_clusterVolumeMounts_and_coreLimitProvided_and_affinityAlreadySet {

    @Mock
    private SparkCluster mockSparkCluster;


    @Test
    public void test_getDriverSpec_clusterVolumeMounts_and_coreLimitProvided_and_affinityAlreadySet() throws Exception {
        // Arrange
        SubmitApplicationRequest req = new SubmitApplicationRequest();
        DriverSpec reqDriver = new DriverSpec();
        reqDriver.setCores(2);
        // trigger branch where coreLimit is parsed and adjusted
        reqDriver.setCoreLimit("1000");
        reqDriver.setMemory("512m");
        // provide a non-empty service account so it won't be overridden
        reqDriver.setServiceAccount("driver-sa");
        // set affinity so createNodeAffinityForSparkPods won't be called
        reqDriver.setAffinity(new Affinity(null));
        req.setDriver(reqDriver);
        // request volumes null or empty so cluster volumes should be used
        req.setVolumes(null);
        AppConfig appConfig = new AppConfig();
        appConfig.setQueues(Collections.emptyList());
        // cluster driver with volume mounts that should be applied
        DriverSpec clusterDriver = new DriverSpec();
        List<VolumeMount> clusterMounts = new ArrayList<>();
        clusterDriver.setVolumeMounts(clusterMounts);
        when(mockSparkCluster.getDriver()).thenReturn(clusterDriver);
        when(mockSparkCluster.getVolumes()).thenReturn(Collections.singletonList(new Volume()));
        when(mockSparkCluster.getSparkServiceAccount()).thenReturn("cluster-sa");
        // Act
        DriverSpec result = ApplicationSubmissionHelper.getDriverSpec(req, appConfig, "any-queue", mockSparkCluster);
        // Assert cores adjusted
        double cpuBuffer = com.apple.spark.core.SparkConstants.DRIVER_CPU_BUFFER_RATIO;
        int expectedAdjustedCores = (int) Math.round(2 * cpuBuffer);
        assertEquals(expectedAdjustedCores, result.getCores());
        // Assert coreLimit parsed and adjusted by buffer ratio (string numeric without 'm' in this branch)
        long originalLimit = 1000L;
        long expectedAdjustedLimit = Math.round(originalLimit * cpuBuffer);
        assertEquals(String.valueOf(expectedAdjustedLimit), result.getCoreLimit());
        // Memory adjusted accordingly
        double memBuffer = com.apple.spark.core.SparkConstants.DRIVER_MEM_BUFFER_RATIO;
        long expectedMemNumber = (long) Math.ceil(512L * memBuffer);
        assertEquals(String.valueOf(expectedMemNumber) + "m", result.getMemory());
        // Service account should remain what was provided in driver
        assertEquals("driver-sa", result.getServiceAccount());
        // Volume mounts should come from cluster driver since request volumes were null
        assertEquals(clusterMounts, result.getVolumeMounts());
        // Affinity should remain unchanged (we set a non-null affinity)
        assertNotNull(result.getAffinity());
    }

}
