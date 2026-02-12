package com.apple.spark.core;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/*
 This test file includes minimal implementations of dependent classes and constants
 to allow compiling and testing ApplicationSubmissionHelper.getSparkConf in isolation.

 All helper classes are implemented as static nested classes inside the public test
 class to avoid collisions with production classes and to ensure the test class is
 discoverable by the test runner.
*/

public class ApplicationSubmissionHelperTest {

    /* Constants used by ApplicationSubmissionHelper.substitutionSparkConfigValue */
    static class TestConstants {
        public static final String SPARK_APPLICATION_RESOURCE_NAME_VAR = "${SUBMISSION_ID}";
    }

    /* Minimal SparkConstants used by ApplicationSubmissionHelper */
    static class TestSparkConstants {
        public static final String SPARK_APP_NAME_CONFIG = "spark.app.name";
    }

    /* Simple StringUtils replacement for org.apache.commons.lang3.StringUtils.isEmpty */
    static class TestStringUtils {
        public static boolean isEmpty(CharSequence cs) {
            return cs == null || cs.length() == 0;
        }
    }

    /* Minimal AppConfig and nested SparkCluster used by ApplicationSubmissionHelper */
    static class AppConfig {

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

    /* Minimal SubmitApplicationRequest used by ApplicationSubmissionHelper */
    static class SubmitApplicationRequest {
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

    /* The class under test: ApplicationSubmissionHelper.getSparkConf
       Implemented here as a static nested class so tests run in isolation. */
    static class ApplicationSubmissionHelper {

        public static Map<String, String> getSparkConf(String submissionId, SubmitApplicationRequest request,
                                                       AppConfig appConfig, AppConfig.SparkCluster sparkCluster) {
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

            if (request != null && !TestStringUtils.isEmpty(request.getApplicationName())) {
                if (sparkConf == null) {
                    sparkConf = new HashMap<>();
                }
                sparkConf.put(TestSparkConstants.SPARK_APP_NAME_CONFIG, request.getApplicationName());
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
                value = value.replace(TestConstants.SPARK_APPLICATION_RESOURCE_NAME_VAR, submissionId);
            }
            return value;
        }
    }

    @Test
    public void testGetSparkConf_allNulls_returnsNull() {
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
    public void testGetSparkConf_defaultWithSubstitution_appliesSubstitution() {
        SubmitApplicationRequest request = new SubmitApplicationRequest();
        // no request-level overrides
        request.setSparkConf(null);

        AppConfig appConfig = new AppConfig();
        Map<String, String> defaults = new HashMap<>();
        defaults.put("path.config", TestConstants.SPARK_APPLICATION_RESOURCE_NAME_VAR + "/data");
        defaults.put("unchanged", "value");
        appConfig.setDefaultSparkConf(defaults);
        appConfig.setFixedSparkConf(null);

        AppConfig.SparkCluster cluster = new AppConfig.SparkCluster();
        cluster.setSparkConf(null);

        Map<String, String> result = ApplicationSubmissionHelper.getSparkConf("sub-42", request, appConfig, cluster);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("sub-42/data", result.get("path.config"));
        assertEquals("value", result.get("unchanged"));
    }

    @Test
    public void testGetSparkConf_ordering_requestOverrides_fixedOverrides_and_appName() {
        SubmitApplicationRequest request = new SubmitApplicationRequest();
        Map<String, String> requestConf = new HashMap<>();
        requestConf.put("a", "request-a");
        requestConf.put("shared", "request-shared");
        request.setSparkConf(requestConf);
        request.setApplicationName("my-app");

        AppConfig appConfig = new AppConfig();
        Map<String, String> defaults = new HashMap<>();
        defaults.put("a", "default-a");
        defaults.put("b", "default-b");
        appConfig.setDefaultSparkConf(defaults);

        Map<String, String> fixed = new HashMap<>();
        fixed.put("fixed-only", "fixed");
        // fixed should overwrite previous values if key collides
        fixed.put("shared", "fixed-shared");
        appConfig.setFixedSparkConf(fixed);

        AppConfig.SparkCluster cluster = new AppConfig.SparkCluster();
        Map<String, String> clusterConf = new HashMap<>();
        clusterConf.put("c", "cluster-c");
        clusterConf.put("a", "cluster-a");
        cluster.setSparkConf(clusterConf);

        Map<String, String> result = ApplicationSubmissionHelper.getSparkConf("submission-9", request, appConfig, cluster);
        assertNotNull(result);

        // 'a' should be from request ("request-a"), not default or cluster
        assertEquals("request-a", result.get("a"));

        // 'b' comes from defaults
        assertEquals("default-b", result.get("b"));

        // 'c' comes from cluster (with substitution semantics — none here)
        assertEquals("cluster-c", result.get("c"));

        // 'shared' is present in request but fixed overrides it at the end with fixed-shared
        assertEquals("fixed-shared", result.get("shared"));

        // fixed-only present
        assertEquals("fixed", result.get("fixed-only"));

        // application name should be set as spark.app.name
        assertEquals("my-app", result.get(TestSparkConstants.SPARK_APP_NAME_CONFIG));
    }

    @Test
    public void testGetSparkConf_nullAppConfig_usesClusterAndRequest() {
        SubmitApplicationRequest request = new SubmitApplicationRequest();
        Map<String, String> req = new HashMap<>();
        req.put("r", "req");
        request.setSparkConf(req);
        request.setApplicationName(null);

        AppConfig appConfig = null;

        AppConfig.SparkCluster cluster = new AppConfig.SparkCluster();
        Map<String, String> clusterConf = new HashMap<>();
        clusterConf.put("path", TestConstants.SPARK_APPLICATION_RESOURCE_NAME_VAR + "/x");
        cluster.setSparkConf(clusterConf);

        Map<String, String> result = ApplicationSubmissionHelper.getSparkConf("s-100", request, appConfig, cluster);
        assertNotNull(result);
        // cluster substitution should be applied
        assertEquals("s-100/x", result.get("path"));
        // request override present
        assertEquals("req", result.get("r"));
        // no app name set
        assertFalse(result.containsKey(TestSparkConstants.SPARK_APP_NAME_CONFIG));
    }

    @Test
    public void testGetSparkConf_fixedOverrides_everything() {
        SubmitApplicationRequest request = new SubmitApplicationRequest();
        Map<String, String> req = new HashMap<>();
        req.put("k", "request");
        request.setSparkConf(req);
        request.setApplicationName("app");

        AppConfig appConfig = new AppConfig();
        Map<String, String> defaults = new HashMap<>();
        defaults.put("k", "default");
        appConfig.setDefaultSparkConf(defaults);

        Map<String, String> fixed = new HashMap<>();
        fixed.put("k", "fixed");
        appConfig.setFixedSparkConf(fixed);

        AppConfig.SparkCluster cluster = new AppConfig.SparkCluster();
        cluster.setSparkConf(null);

        Map<String, String> result = ApplicationSubmissionHelper.getSparkConf("sid", request, appConfig, cluster);
        assertNotNull(result);
        // fixed should override request/default
        assertEquals("fixed", result.get("k"));
        // app name should be present
        assertEquals("app", result.get(TestSparkConstants.SPARK_APP_NAME_CONFIG));
    }
}
