package com.apple.spark.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/*
 Minimal, self-contained unit test file for testing the private static
 substitutionSparkConfigValue method in ApplicationSubmissionHelper.
*/

class Constants {
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

/* Minimal AppConfig and nested SparkCluster for the purposes of compiling/testing */
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

/* Minimal SubmitApplicationRequest with only the used getters/setters */
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

/* The class under test with a minimal implementation of getSparkConf and the private substitution method */
public class ApplicationSubmissionHelper {

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

    // private method under test
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
    void testSubstitutionSparkConfigValue_privateMethod_viaReflection() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
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
}
