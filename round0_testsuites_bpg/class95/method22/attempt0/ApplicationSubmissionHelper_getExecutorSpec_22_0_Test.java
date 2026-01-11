package com.apple.spark.core;

import com.apple.spark.AppConfig;
import com.apple.spark.api.SubmitApplicationRequest;
import com.apple.spark.operator.ExecutorSpec;
import com.apple.spark.operator.Volume;
import io.fabric8.kubernetes.api.model.PodDNSConfigOption;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
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
import com.apple.spark.operator.Affinity;
import com.apple.spark.operator.BatchSchedulerConfiguration;
import com.apple.spark.operator.DriverSpec;
import com.apple.spark.operator.NodeAffinity;
import com.apple.spark.operator.SparkApplicationSpec;
import com.apple.spark.operator.SparkUIConfiguration;
import com.apple.spark.util.ExceptionUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import io.fabric8.kubernetes.api.model.PodDNSConfig;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationSubmissionHelper_getExecutorSpec_22_0_Test {

    @Test
    public void testGetExecutorSpec_AdjustsCoresCoreLimitMemoryAndSetsAnnotationsAndDnsConfig() throws Exception {
        // Prepare SubmitApplicationRequest with executor
        SubmitApplicationRequest request = new SubmitApplicationRequest();
        ExecutorSpec exec = new ExecutorSpec();
        // original cores
        exec.setCores(3);
        // original memory
        exec.setMemory("512m");
        // set a label that would be normalized by KubernetesHelper
        Map<String, String> labels = new HashMap<>();
        // get DAG_NAME_LABEL and TASK_NAME_LABEL from Constants via reflection
        Class<?> constantsCls = Class.forName("com.apple.spark.core.Constants");
        String dagNameLabel = (String) constantsCls.getField("DAG_NAME_LABEL").get(null);
        String taskNameLabel = (String) constantsCls.getField("TASK_NAME_LABEL").get(null);
        labels.put(dagNameLabel, "!badLabelStart");
        labels.put("other", "value");
        // set labels on executor and attach to request
        exec.setLabels(labels);
        // set executor coreLimit null to force default core limit calculation
        exec.setCoreLimit(null);
        request.setExecutor(exec);
        // Add volumes to request to ensure sparkCluster is not accessed (avoids null)
        request.setVolumes(Collections.singletonList(new Volume()));
        // Create AppConfig with empty queues so private helper returns defaults
        AppConfig appConfig = new AppConfig();
        appConfig.setQueues(Collections.emptyList());
        String parentQueue = "anyQueue";
        // Call the focal method
        // not used due to request volumes
        com.apple.spark.AppConfig.SparkCluster sparkCluster = null;
        com.apple.spark.operator.ExecutorSpec result = ApplicationSubmissionHelper.getExecutorSpec(request, appConfig, parentQueue, sparkCluster);
        assertNotNull(result);
        // Use reflection to get private buffer ratios
        Method getCpuRatio = ApplicationSubmissionHelper.class.getDeclaredMethod("getExecutorCPUBufferForQueue", AppConfig.class, String.class);
        getCpuRatio.setAccessible(true);
        double cpuRatio = (Double) getCpuRatio.invoke(null, appConfig, parentQueue);
        Method getMemRatio = ApplicationSubmissionHelper.class.getDeclaredMethod("getExecutorMemBufferForQueue", AppConfig.class, String.class);
        getMemRatio.setAccessible(true);
        double memRatio = (Double) getMemRatio.invoke(null, appConfig, parentQueue);
        // Check cores adjusted: adjustedExecutorCores = round(originalExecutorCores * cpuRatio)
        int expectedAdjustedCores = (int) Math.round(3 * cpuRatio);
        assertEquals(expectedAdjustedCores, result.getCores().intValue());
        // CORE_LIMIT_RATIO from SparkConstants
        Class<?> sparkConstCls = Class.forName("com.apple.spark.core.SparkConstants");
        double coreLimitRatio = ((Number) sparkConstCls.getField("CORE_LIMIT_RATIO").get(null)).doubleValue();
        // core limit should be set because original was null; it uses adjusted cores
        long expectedCoreLimit = (// see focal method logic
        long) // see focal method logic
        Math.ceil(expectedAdjustedCores * coreLimitRatio * cpuRatio);
        String expectedCoreLimitStr = expectedCoreLimit + "m";
        assertEquals(expectedCoreLimitStr, result.getCoreLimit());
        // Memory adjusted: original 512 -> getMemNumFromRequestStr -> 512 -> ceil(512 * memRatio)
        Method getMemNum = ApplicationSubmissionHelper.class.getDeclaredMethod("getMemNumFromRequestStr", String.class);
        getMemNum.setAccessible(true);
        long originalMemNum = (Long) getMemNum.invoke(null, "512m");
        long expectedAdjustedMem = (long) Math.ceil(originalMemNum * memRatio);
        Method getMemUnit = ApplicationSubmissionHelper.class.getDeclaredMethod("getMemUnitFromRequestStr", String.class);
        getMemUnit.setAccessible(true);
        String memUnit = (String) getMemUnit.invoke(null, "512m");
        assertEquals(expectedAdjustedMem + memUnit, result.getMemory());
        // Annotations should have autoscaler scale-in key/value when result.getAnnotations() was null initially
        @SuppressWarnings("unchecked")
        Map<String, String> annotations = (Map<String, String>) result.getAnnotations();
        assertNotNull(annotations);
        String autoscalerKey = (String) constantsCls.getField("KUBE_CLUSTER_AUTOSCALER_SCALE_IN_ANNOTATION_KEY").get(null);
        String autoscalerVal = (String) constantsCls.getField("KUBE_CLUSTER_AUTOSCALER_SCALE_IN_ANNOTATION_VALUE").get(null);
        assertEquals(autoscalerVal, annotations.get(autoscalerKey));
        // DNS config
        assertNotNull(result.getDnsConfig());
        assertNotNull(result.getDnsConfig().getOptions());
        assertFalse(result.getDnsConfig().getOptions().isEmpty());
        PodDNSConfigOption opt = result.getDnsConfig().getOptions().get(0);
        String dnsName = (String) constantsCls.getField("DNS_CONFIG_OPTION_NDOTS_NAME").get(null);
        String dnsValue = (String) constantsCls.getField("DNS_CONFIG_OPTION_NDOTS_VALUE").get(null);
        assertEquals(dnsName, opt.getName());
        assertEquals(dnsValue, opt.getValue());
        // Labels normalization: request labels should have been normalized via KubernetesHelper.normalizeLabelValue
        Map<String, String> resultingLabels = result.getLabels();
        assertNotNull(resultingLabels);
        // '!' replaced by '0' per logic
        assertEquals("0badLabelStart", resultingLabels.get(dagNameLabel));
        assertEquals("value", resultingLabels.get("other"));
    }

    @Test
    public void testGetExecutorSpec_WithExistingCoreLimit_AdjustsCoreLimitOnly() throws Exception {
        SubmitApplicationRequest request = new SubmitApplicationRequest();
        ExecutorSpec exec = new ExecutorSpec();
        exec.setCores(4);
        exec.setMemory("200m");
        // original core limit present (string)
        exec.setCoreLimit("1200");
        request.setExecutor(exec);
        // ensure volumes list not present and sparkCluster not used by making volumes non-empty in request
        request.setVolumes(Collections.singletonList(new Volume()));
        AppConfig appConfig = new AppConfig();
        appConfig.setQueues(Collections.emptyList());
        String parentQueue = "q";
        com.apple.spark.AppConfig.SparkCluster sparkCluster = null;
        ExecutorSpec result = ApplicationSubmissionHelper.getExecutorSpec(request, appConfig, parentQueue, sparkCluster);
        // compute expected using private cpu ratio method
        Method getCpuRatio = ApplicationSubmissionHelper.class.getDeclaredMethod("getExecutorCPUBufferForQueue", AppConfig.class, String.class);
        getCpuRatio.setAccessible(true);
        double cpuRatio = (Double) getCpuRatio.invoke(null, appConfig, parentQueue);
        // originalExecutorCoreLimit = Long.parseLong("1200")
        long originalCoreLimit = 1200L;
        long expectedAdjustedExecutorCoreLimit = Math.round(originalCoreLimit * cpuRatio);
        assertEquals(String.valueOf(expectedAdjustedExecutorCoreLimit), result.getCoreLimit());
    }

    @Test
    public void testPrivateMemParsingMethods() throws Exception {
        Method getMemUnit = ApplicationSubmissionHelper.class.getDeclaredMethod("getMemUnitFromRequestStr", String.class);
        Method getMemNum = ApplicationSubmissionHelper.class.getDeclaredMethod("getMemNumFromRequestStr", String.class);
        getMemUnit.setAccessible(true);
        getMemNum.setAccessible(true);
        String unit = (String) getMemUnit.invoke(null, "123.45Mi");
        long num = (Long) getMemNum.invoke(null, "123.45Mi");
        assertEquals("Mi", unit);
        // (long) Double.parseDouble("123.45") => 123
        assertEquals(123L, num);
    }
}
