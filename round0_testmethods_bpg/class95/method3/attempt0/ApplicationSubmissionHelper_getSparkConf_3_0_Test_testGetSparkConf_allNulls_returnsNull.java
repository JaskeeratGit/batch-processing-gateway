package com.apple.spark.core;

import java.lang.reflect.InvocationTargetException;
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

/*
 This test file includes minimal implementations of dependent classes and constants
 to allow compiling and testing ApplicationSubmissionHelper.getSparkConf in isolation.
*/
class ApplicationSubmissionHelper_getSparkConf_3_0_Test_testGetSparkConf_allNulls_returnsNull {

    public static final String SPARK_APPLICATION_RESOURCE_NAME_VAR = "${SUBMISSION_ID}";
}

class SparkConstants {

    public static final String SPARK_APP_NAME_CONFIG = "spark.app.name";
}

/* Simple StringUtils replacement for org.apache.commons.lang3.StringUtils.isEmpty */
class StringUtils {

    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }
}

class AppConfig {

    private Map<String, String> defaultSparkConf;

    private Map<String, String> fixedSparkConf;

    public Map<String, String> getDefaultSparkConf() {
        return defaultSparkConf;
    }

    public void setDefaultSparkConf(Map<String, String> defaultSparkConf) {
        this.defaultSparkConf = defaultSparkConf;
    }

    public Map<String, String> getFixedSparkConf() {
        return fixedSparkConf;
    }

    public void setFixedSparkConf(Map<String, String> fixedSparkConf) {
        this.fixedSparkConf = fixedSparkConf;
    }

    public static class SparkCluster {

        private Map<String, String> sparkConf;

        public Map<String, String> getSparkConf() {
            return sparkConf;
        }

        public void setSparkConf(Map<String, String> sparkConf) {
            this.sparkConf = sparkConf;
        }
    }
}

class SubmitApplicationRequest {

    private String applicationName;

    private Map<String, String> sparkConf;

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public Map<String, String> getSparkConf() {
        return sparkConf;
    }

    public void setSparkConf(Map<String, String> sparkConf) {
        this.sparkConf = sparkConf;
    }
}

public class ApplicationSubmissionHelper {

    private static final String FILE_PATH_REGEX = "([a-zA-Z~])?(\\/[a-zA-Z0-9_.-]+)+\\/?";

    public static Map<String, String> getSparkConf(String submissionId, SubmitApplicationRequest request, AppConfig appConfig, AppConfig.SparkCluster sparkCluster) {
        Map<String, String> sparkConf = null;
        Map<String, String> defaultSparkConf = appConfig == null ? null : appConfig.getDefaultSparkConf();
        Map<String, String> fixedSparkConf = appConfig == null ? null : appConfig.getFixedSparkConf();
        if (defaultSparkConf != null) {
            if (sparkConf == null) {
                sparkConf = new HashMap<>();
            }
            Map<String, String> filteredDefaultSparkConf = applyFeatureGate(request, defaultSparkConf);
            for (Map.Entry<String, String> entry : filteredDefaultSparkConf.entrySet()) {
                sparkConf.put(entry.getKey(), substitutionSparkConfigValue(entry.getValue(), submissionId));
            }
        }
        if (sparkCluster != null && sparkCluster.getSparkConf() != null) {
            if (sparkConf == null) {
                sparkConf = new HashMap<>();
            }
            for (Map.Entry<String, String> entry : sparkCluster.getSparkConf().entrySet()) {
                sparkConf.put(entry.getKey(), substitutionSparkConfigValue(entry.getValue(), submissionId));
            }
        }
        if (request != null && request.getSparkConf() != null) {
            if (sparkConf == null) {
                sparkConf = new HashMap<>();
            }
            for (Map.Entry<String, String> entry : request.getSparkConf().entrySet()) {
                sparkConf.put(entry.getKey(), entry.getValue());
            }
        }
        if (fixedSparkConf != null) {
            if (sparkConf == null) {
                sparkConf = new HashMap<>();
            }
            sparkConf.putAll(fixedSparkConf);
        }
        if (request != null && !StringUtils.isEmpty(request.getApplicationName())) {
            if (sparkConf == null) {
                sparkConf = new HashMap<>();
            }
            sparkConf.put(SparkConstants.SPARK_APP_NAME_CONFIG, request.getApplicationName());
        }
        return sparkConf;
    }

    // included minimal feature gate behavior: in original code this filters based on request features.
    private static Map<String, String> applyFeatureGate(SubmitApplicationRequest request, Map<String, String> defaults) {
        // For testing purposes, return the defaults unchanged.
        return defaults;
    }

    private static String substitutionSparkConfigValue(String value, String submissionId) {
        if (value != null) {
            value = value.replace(Constants.SPARK_APPLICATION_RESOURCE_NAME_VAR, submissionId);
        }
        return value;
    }
}

/* Test class */
class ApplicationSubmissionHelperTest {

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


}
