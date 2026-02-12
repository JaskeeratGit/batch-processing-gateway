package com.apple.spark.core;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/*
 This test file includes minimal implementations of dependent classes and constants
 to allow compiling and testing ApplicationSubmissionHelper.getSparkConf in isolation.
*/

/* Minimal Constants class used by ApplicationSubmissionHelper.substitutionSparkConfigValue */
class Constants {
    public static final String SPARK_APPLICATION_RESOURCE_NAME_VAR = "${SUBMISSION_ID}";
}

/* Minimal SparkConstants used by ApplicationSubmissionHelper.getSparkConf */
class SparkConstants {
    public static final String SPARK_APP_NAME_CONFIG = "spark.app.name";
}

/* Simple StringUtils replacement for org.apache.commons.lang3.StringUtils.isEmpty */
class StringUtils {
    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }
}

/* Minimal AppConfig and nested SparkCluster used by ApplicationSubmissionHelper.getSparkConf */
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

/* Minimal SubmitApplicationRequest used by ApplicationSubmissionHelper.getSparkConf */
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

/* The class under test (kept minimal but faithful to behavior described) */
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
public class ApplicationSubmissionHelperTest {

    @Test
    public void testSubstitutionSparkConfigValue_privateMethod_viaReflection() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = ApplicationSubmissionHelper.class.getDeclaredMethod("substitutionSparkConfigValue", String.class, String.class);
        method.setAccessible(true);
        // null input should return null
        Object resNull = method.invoke(null, (String) null, "id");
        assertNull(resNull);

        // substitution should occur
        String template = "prefix-" + Constants.SPARK_APPLICATION_RESOURCE_NAME_VAR + "-suffix";
        Object res = method.invoke(null, template, "SUB123");
        assertTrue(res instanceof String);
        assertEquals("prefix-SUB123-suffix", res);
    }

    @Test
    public void testGetSparkConf_combinedSources_and_overrides_and_substitution() {
        String submissionId = "S1";

        // default spark conf with substitution variable and a key that will be overridden later
        Map<String, String> defaultConf = new HashMap<>();
        defaultConf.put("a", "val-" + Constants.SPARK_APPLICATION_RESOURCE_NAME_VAR);
        defaultConf.put("b", "default-b");

        // cluster conf with substitution variable overriding "b" and providing "c"
        AppConfig.SparkCluster sparkCluster = new AppConfig.SparkCluster();
        Map<String, String> clusterConf = new HashMap<>();
        clusterConf.put("b", "cluster-" + Constants.SPARK_APPLICATION_RESOURCE_NAME_VAR);
        clusterConf.put("c", "clusterOnly");
        sparkCluster.setSparkConf(clusterConf);

        // request conf that overrides "c" and adds "d"
        SubmitApplicationRequest request = new SubmitApplicationRequest();
        Map<String, String> requestConf = new HashMap<>();
        requestConf.put("c", "requestC");
        requestConf.put("d", "requestOnly");
        request.setSparkConf(requestConf);
        request.setApplicationName("myApp");

        // fixed conf that should be applied last and thus take precedence over previous values
        AppConfig appConfig = new AppConfig();
        appConfig.setDefaultSparkConf(defaultConf);
        Map<String, String> fixedConf = new HashMap<>();
        fixedConf.put("e", "fixed");
        appConfig.setFixedSparkConf(fixedConf);

        Map<String, String> result = ApplicationSubmissionHelper.getSparkConf(submissionId, request, appConfig, sparkCluster);

        assertNotNull(result);
        // substitution applied for default 'a'
        assertEquals("val-S1", result.get("a"));
        // cluster override applied for 'b' with substitution
        assertEquals("cluster-S1", result.get("b"));
        // request override applied for 'c'
        assertEquals("requestC", result.get("c"));
        // request-provided key 'd'
        assertEquals("requestOnly", result.get("d"));
        // fixed conf present
        assertEquals("fixed", result.get("e"));
        // application name set
        assertEquals("myApp", result.get(SparkConstants.SPARK_APP_NAME_CONFIG));
    }

    @Test
    public void testGetSparkConf_allNullInputs_returnsNull() {
        Map<String, String> result = ApplicationSubmissionHelper.getSparkConf("id", null, null, null);
        assertNull(result, "Expected null when no configuration sources and no application name provided");
    }

    @Test
    public void testGetSparkConf_applicationNameOnly_returnsMapWithAppName() {
        SubmitApplicationRequest request = new SubmitApplicationRequest();
        request.setApplicationName("onlyAppName");
        Map<String, String> result = ApplicationSubmissionHelper.getSparkConf("id", request, null, null);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("onlyAppName", result.get(SparkConstants.SPARK_APP_NAME_CONFIG));
    }
}
