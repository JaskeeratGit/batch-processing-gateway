package com.apple.spark.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.HashMap;
import java.util.Map;

/* Minimal constants used by the tested code */
public class Constants {
    public static final String SPARK_APPLICATION_RESOURCE_NAME_VAR = "${SUBMISSION_ID}";
    // Added missing constants referenced by other tests
    public static final String QUEUE_INFO = "queueInfo";
    public static final String SERVICE_ABBR = "serviceAbbr";
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

// Make ApplicationSubmissionHelper package-private to avoid public class filename mismatch
class ApplicationSubmissionHelper {

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

    // Minimal feature gate behavior for testing.
    private static Map<String, String> applyFeatureGate(SubmitApplicationRequest request, Map<String, String> defaults) {
        return defaults;
    }

    private static String substitutionSparkConfigValue(String value, String submissionId) {
        if (value != null) {
            return value.replace(Constants.SPARK_APPLICATION_RESOURCE_NAME_VAR, submissionId);
        }
        return null;
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
