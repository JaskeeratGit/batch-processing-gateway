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

public class ApplicationSubmissionHelper_getExecutorSpec_22_0_Test_testGetExecutorSpec_WithExistingCoreLimit_AdjustsCoreLimitOnly {


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

}
