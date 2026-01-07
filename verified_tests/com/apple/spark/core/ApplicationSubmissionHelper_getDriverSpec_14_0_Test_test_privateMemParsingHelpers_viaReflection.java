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
public class ApplicationSubmissionHelper_getDriverSpec_14_0_Test_test_privateMemParsingHelpers_viaReflection {

    @Mock
    private SparkCluster mockSparkCluster;



    @Test
    public void test_privateMemParsingHelpers_viaReflection() throws Exception {
        // Access private methods getMemUnitFromRequestStr and getMemNumFromRequestStr using reflection
        Method getUnit = ApplicationSubmissionHelper.class.getDeclaredMethod("getMemUnitFromRequestStr", String.class);
        getUnit.setAccessible(true);
        Method getNum = ApplicationSubmissionHelper.class.getDeclaredMethod("getMemNumFromRequestStr", String.class);
        getNum.setAccessible(true);
        String memStr1 = "1024m";
        String unit1 = (String) getUnit.invoke(null, memStr1);
        long num1 = (Long) getNum.invoke(null, memStr1);
        assertEquals("m", unit1);
        assertEquals(1024L, num1);
        String memStr2 = "1.5g";
        String unit2 = (String) getUnit.invoke(null, memStr2);
        long num2 = (Long) getNum.invoke(null, memStr2);
        assertEquals("g", unit2);
        // 1.5 -> parsed to double then cast to long -> 1
        assertEquals(1L, num2);
    }
}
