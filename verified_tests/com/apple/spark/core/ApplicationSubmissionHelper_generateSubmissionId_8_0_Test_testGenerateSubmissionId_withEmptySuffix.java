package com.apple.spark.core;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import java.util.stream.Collectors;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationSubmissionHelper_generateSubmissionId_8_0_Test_testGenerateSubmissionId_withEmptySuffix {

    private static final Pattern UUID_32_HEX = Pattern.compile("^([0-9a-f]{32})$");

    private String invokeGenerateSubmissionId(String clusterId, String submissionIdSuffix) throws Exception {
        Method m = ApplicationSubmissionHelper.class.getDeclaredMethod("generateSubmissionId", String.class, String.class);
        m.setAccessible(true);
        return (String) m.invoke(null, clusterId, submissionIdSuffix);
    }



    @Test
    public void testGenerateSubmissionId_withEmptySuffix() throws Exception {
        String clusterId = "clusterEmpty";
        String result = invokeGenerateSubmissionId(clusterId, "");
        assertNotNull(result, "Result should not be null");
        // Empty suffix should be treated like null (no extra suffix appended)
        String regex = "^" + Pattern.quote(clusterId) + "-([0-9a-f]{32})$";
        assertTrue(result.matches(regex), "Result should match pattern clusterId-32hex when suffix is empty. Actual: " + result);
        String uuidPart = result.substring(clusterId.length() + 1);
        Matcher m = UUID_32_HEX.matcher(uuidPart);
        assertTrue(m.matches(), "UUID part must be 32 lowercase hex chars: " + uuidPart);
    }
}
