package com.apple.spark.core;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
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

public class ApplicationSubmissionHelper_generateSubmissionId_7_0_Test {

    private static final Pattern HEX32 = Pattern.compile("^[0-9a-f]{32}$");

    private Method getGenerateSubmissionIdMethod() throws Exception {
        Method m = ApplicationSubmissionHelper.class.getDeclaredMethod("generateSubmissionId", String.class);
        m.setAccessible(true);
        return m;
    }

    @Test
    public void testGenerateSubmissionId_withNormalClusterId() throws Exception {
        Method m = getGenerateSubmissionIdMethod();
        String clusterId = "clusterA";
        String id = (String) m.invoke(null, clusterId);
        Assertions.assertNotNull(id, "id should not be null");
        Assertions.assertTrue(id.startsWith(clusterId + "-"), "id should start with clusterId and dash");
        String uuidPart = id.substring(clusterId.length() + 1);
        Assertions.assertEquals(32, uuidPart.length(), "UUID part should be 32 chars after removing hyphens");
        Assertions.assertTrue(HEX32.matcher(uuidPart).matches(), "UUID part should be 32 lowercase hex chars");
        Assertions.assertFalse(uuidPart.contains("-"), "UUID part should contain no hyphens");
    }

    @Test
    public void testGenerateSubmissionId_withNullClusterId() throws Exception {
        Method m = getGenerateSubmissionIdMethod();
        String id = (String) m.invoke(null, new Object[] { null });
        Assertions.assertNotNull(id, "id should not be null even if clusterId is null");
        Assertions.assertTrue(id.startsWith("null-"), "null clusterId should be represented as literal \"null-\"");
        String uuidPart = id.substring("null".length() + 1);
        Assertions.assertEquals(32, uuidPart.length());
        Assertions.assertTrue(HEX32.matcher(uuidPart).matches());
    }

    @Test
    public void testGenerateSubmissionId_withEmptyClusterId() throws Exception {
        Method m = getGenerateSubmissionIdMethod();
        String id = (String) m.invoke(null, "");
        Assertions.assertNotNull(id);
        // When clusterId is empty, the format produces a leading dash
        Assertions.assertTrue(id.startsWith("-"), "empty clusterId should produce leading dash");
        String uuidPart = id.substring(1);
        Assertions.assertEquals(32, uuidPart.length());
        Assertions.assertTrue(HEX32.matcher(uuidPart).matches());
    }

    @Test
    public void testGenerateSubmissionId_uniquenessAcrossInvocations() throws Exception {
        Method m = getGenerateSubmissionIdMethod();
        String clusterId = "clusterB";
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            String id = (String) m.invoke(null, clusterId);
            Assertions.assertTrue(id.startsWith(clusterId + "-"));
            ids.add(id);
        }
        // Expect all generated ids to be unique in this small sample
        Assertions.assertEquals(10, ids.size(), "Generated ids should be unique across invocations");
    }

    @Test
    public void testGenerateSubmissionId_preservesClusterContent() throws Exception {
        Method m = getGenerateSubmissionIdMethod();
        String clusterId = "clu-ster_123";
        String id = (String) m.invoke(null, clusterId);
        Assertions.assertNotNull(id);
        // ensure the original clusterId content is preserved at the start
        Assertions.assertTrue(id.startsWith(clusterId + "-"), "original clusterId should be preserved as prefix");
        String uuidPart = id.substring(clusterId.length() + 1);
        Assertions.assertEquals(32, uuidPart.length());
        Assertions.assertTrue(HEX32.matcher(uuidPart).matches());
    }
}
