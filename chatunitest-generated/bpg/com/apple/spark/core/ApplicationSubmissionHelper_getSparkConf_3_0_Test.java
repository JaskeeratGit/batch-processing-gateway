package com.apple.spark.core;

import com.apple.spark.AppConfig;
import com.apple.spark.api.SubmitApplicationRequest;
import java.lang.reflect.Method;
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

public class ApplicationSubmissionHelper_getSparkConf_3_0_Test {

    @Test
    void testGetSparkConf_allNulls_returnsNull() {
        SubmitApplicationRequest request = new SubmitApplicationRequest();
        request.setApplicationName(null);
        request.setSparkConf(null);
        AppConfig appConfig = new AppConfig();
        appConfig.setDefaultSparkConf(null);
        appConfig.setFixedSparkConf(null);
        AppConfig.SparkCluster cluster = new AppConfig.SparkCluster();
        cluster.setSparkConf(null);
        Map<String, String> result = ApplicationSubmissionHelper.getSparkConf("submission-1", request, appConfig, cluster);
        assertNull(result, "When no configs provided, expected null map");
    }

    @Test
    void testGetSparkConf_mergingOrder_substitution_and_overrides() {
        String submissionId = "my-sub";
        Map<String, String> defaultConf = new HashMap<>();
        defaultConf.put("d1", Constants.SPARK_APPLICATION_RESOURCE_NAME_VAR + "_X");
        defaultConf.put("k", "defaultK");
        Map<String, String> clusterConf = new HashMap<>();
        clusterConf.put("d1", "clusterOverride");
        clusterConf.put("c1", Constants.SPARK_APPLICATION_RESOURCE_NAME_VAR + "_C");
        Map<String, String> requestConf = new HashMap<>();
        requestConf.put("c1", "reqOverrideC");
        requestConf.put("r1", "reqVal");
        Map<String, String> fixedConf = new HashMap<>();
        fixedConf.put("r1", "fixedOverrideReq");
        fixedConf.put("fixed", "fixedVal");
        AppConfig appConfig = new AppConfig();
        appConfig.setDefaultSparkConf(defaultConf);
        appConfig.setFixedSparkConf(fixedConf);
        AppConfig.SparkCluster cluster = new AppConfig.SparkCluster();
        cluster.setSparkConf(clusterConf);
        SubmitApplicationRequest request = new SubmitApplicationRequest();
        request.setSparkConf(requestConf);
        request.setApplicationName("myAppName");
        Map<String, String> result = ApplicationSubmissionHelper.getSparkConf(submissionId, request, appConfig, cluster);
        assertNotNull(result, "Resulting sparkConf should not be null when some config provided");
        assertEquals("clusterOverride", result.get("d1"), "cluster should override default for key d1");
        assertEquals("reqOverrideC", result.get("c1"), "request should override cluster for key c1");
        assertEquals("fixedOverrideReq", result.get("r1"), "fixed should override request for key r1");
        assertEquals("fixedVal", result.get("fixed"), "fixed key should be present with fixed value");
        assertEquals("defaultK", result.get("k"), "default-only key should be present");
        assertEquals("myAppName", result.get(SparkConstants.SPARK_APP_NAME_CONFIG), "applicationName should be inserted as spark.app.name");
    }

    @Test
    void testSubstitutionSparkConfigValue_privateMethod_viaReflection() throws Exception {
        Method method = ApplicationSubmissionHelper.class.getDeclaredMethod("substitutionSparkConfigValue", String.class, String.class);
        method.setAccessible(true);
        Object resNull = method.invoke(null, new Object[] { null, "id" });
        assertNull(resNull);
        String template = "prefix-" + Constants.SPARK_APPLICATION_RESOURCE_NAME_VAR + "-suffix";
        Object res = method.invoke(null, new Object[] { template, "SUB123" });
        assertTrue(res instanceof String);
        assertEquals("prefix-SUB123-suffix", res);
    }
}
