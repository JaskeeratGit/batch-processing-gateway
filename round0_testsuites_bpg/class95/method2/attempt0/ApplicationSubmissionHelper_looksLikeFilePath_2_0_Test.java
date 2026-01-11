package com.apple.spark.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
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
import java.util.Collections;
import java.util.HashMap;
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

class ApplicationSubmissionHelper_looksLikeFilePath_2_0_Test {

    @Test
    void testValidFilePaths() {
        // single leading letter allowed
        List<String> // single leading letter allowed
        valid = // single leading letter allowed
        Arrays.// single leading letter allowed
        asList(// single leading letter allowed
        "/foo", // single leading letter allowed
        "/foo/bar", // single leading letter allowed
        "/.hidden", // single leading letter allowed
        "/file123", // single leading letter allowed
        "/a-b.c_d", // single leading letter (upper-case)
        "/file/", // leading tilde allowed
        "a/foo", "Z/foo", "~/file");
        for (String s : valid) {
            assertTrue(ApplicationSubmissionHelper.looksLikeFilePath(s), "Expected to match as file path: " + s);
        }
    }

    @Test
    void testInvalidFilePaths() {
        // empty
        List<String> // empty
        invalid = // empty
        Arrays.// slash alone
        asList(// no leading slash or single-letter prefix
        "", // leading more than one letter not allowed
        "/", // empty segment
        "relative/path", // invalid character '!'
        "ab/foo", // leading space
        "a//foo", // single letter only (needs /segment)
        "/foo/!bar", // missing leading slash (dot-only segment without leading /)
        " /leadingSpace", "a", ".hidden");
        for (String s : invalid) {
            assertFalse(ApplicationSubmissionHelper.looksLikeFilePath(s), "Expected NOT to match as file path: " + s);
        }
    }

    @Test
    void testNullInputThrowsNullPointerException_Direct() {
        assertThrows(NullPointerException.class, () -> ApplicationSubmissionHelper.looksLikeFilePath(null));
    }

    @Test
    void testReflectionInvokeStaticMethod_validAndNull() throws Exception {
        Method m = ApplicationSubmissionHelper.class.getDeclaredMethod("looksLikeFilePath", String.class);
        // harmless for public method; covers reflection usage requirement
        m.setAccessible(true);
        // valid invocation via reflection
        Object result = m.invoke(null, "/reflect/test");
        assertTrue(result instanceof Boolean);
        assertTrue((Boolean) result);
        // invoking with null through reflection results in InvocationTargetException whose cause is NPE
        InvocationTargetException ite = assertThrows(InvocationTargetException.class, () -> m.invoke(null, new Object[] { null }));
        assertNotNull(ite.getCause());
        assertTrue(ite.getCause() instanceof NullPointerException);
    }
}
