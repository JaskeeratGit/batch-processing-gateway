package com.apple.spark.core;

import com.apple.spark.api.SubmitApplicationRequest;
import javax.ws.rs.WebApplicationException;
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
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationSubmissionHelper_parseSubmitRequest_0_0_Test {

    @Test
    public void testParseJsonSuccess_emptyObject() {
        String json = "{}";
        SubmitApplicationRequest req = ApplicationSubmissionHelper.parseSubmitRequest(json, null);
        assertNotNull(req, "Parsed request should not be null for valid JSON object");
    }

    @Test
    public void testParseYamlSuccess_emptyObject() {
        String yaml = "{}";
        SubmitApplicationRequest req = ApplicationSubmissionHelper.parseSubmitRequest(yaml, "application/x-yaml");
        assertNotNull(req, "Parsed request should not be null for valid YAML object");
    }

    @Test
    public void testParseJsonInvalid_looksLikeFilePath_throwsBadRequest() {
        String filePath = "/tmp/some-file.txt";
        WebApplicationException ex = assertThrows(WebApplicationException.class, () -> ApplicationSubmissionHelper.parseSubmitRequest(filePath, null));
        assertTrue(ex.getMessage().contains("looks like a file path") || ex.getMessage().toLowerCase().contains("file path"), "Exception message should mention that the request looks like a file path");
        assertEquals(400, ex.getResponse().getStatus());
    }

    @Test
    public void testParseJsonInvalid_notFilePath_throwsBadRequest() {
        String badJson = "not a valid json";
        WebApplicationException ex = assertThrows(WebApplicationException.class, () -> ApplicationSubmissionHelper.parseSubmitRequest(badJson, null));
        assertTrue(ex.getMessage().startsWith("Invalid JSON request:"), "Exception message should start with 'Invalid JSON request:'");
        assertEquals(400, ex.getResponse().getStatus());
    }

    @Test
    public void testParseYamlInvalid_throwsBadRequest() {
        String badYaml = "key: [unclosed";
        WebApplicationException ex = assertThrows(WebApplicationException.class, () -> ApplicationSubmissionHelper.parseSubmitRequest(badYaml, "text/yaml"));
        assertTrue(ex.getMessage().startsWith("Invalid YAML request:"), "Exception message should start with 'Invalid YAML request:'");
        assertEquals(400, ex.getResponse().getStatus());
    }

    @Test
    public void testLooksLikeFilePath_reflection_true() throws Exception {
        Method m = ApplicationSubmissionHelper.class.getDeclaredMethod("looksLikeFilePath", String.class);
        m.setAccessible(true);
        Object result = m.invoke(null, "/var/log/app.log");
        assertTrue(result instanceof Boolean && (Boolean) result, "Expected true for a valid file path");
    }

    @Test
    public void testLooksLikeFilePath_reflection_false() throws Exception {
        Method m = ApplicationSubmissionHelper.class.getDeclaredMethod("looksLikeFilePath", String.class);
        m.setAccessible(true);
        Object result = m.invoke(null, "not/a valid path with spaces");
        assertTrue(result instanceof Boolean && !((Boolean) result), "Expected false for a non-file-path string");
    }
}
