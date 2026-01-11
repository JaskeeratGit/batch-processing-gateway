package com.apple.spark.core;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
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
import java.util.List;
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

/**
 * JUnit5 tests for ApplicationSubmissionHelper.getProxyUser(...)
 */
public class ApplicationSubmissionHelper_getProxyUser_1_0_Test {

    private Field airflowField;

    private Object originalAirflowValue;

    private Field modifiersField;

    @BeforeEach
    public void setUp() throws Exception {
        // Obtain the Constants class and the AIRFLOW_SYSTEM_ACCOUNTS field via reflection.
        Class<?> constantsClass = Class.forName("com.apple.spark.core.Constants");
        airflowField = constantsClass.getDeclaredField("AIRFLOW_SYSTEM_ACCOUNTS");
        airflowField.setAccessible(true);
        // Save original value to restore later
        originalAirflowValue = airflowField.get(null);
        // Remove final modifier if present so we can replace the Set for test purposes
        modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(airflowField, airflowField.getModifiers() & ~Modifier.FINAL);
        // Replace with a predictable set for testing
        Set<String> testSet = new HashSet<>(Arrays.asList("airflow-system", "airflow"));
        airflowField.set(null, testSet);
    }

    @AfterEach
    public void tearDown() throws Exception {
        // Restore original value (safe even if original was null)
        if (airflowField != null) {
            airflowField.set(null, originalAirflowValue);
        }
        if (modifiersField != null) {
            modifiersField.setAccessible(false);
        }
    }

    @Test
    public void testGetProxyUser_whenUserIsInAirflowAccounts_returnsDagUser() {
        String user = "airflow";
        String dagUser = "dagOwner";
        String result = ApplicationSubmissionHelper.getProxyUser(user, dagUser);
        assertEquals(dagUser, result, "If user is an airflow system account, should return dagUser");
    }

    @Test
    public void testGetProxyUser_whenUserIsInAirflowAccountsDifferentEntry_returnsDagUser() {
        String user = "airflow-system";
        String dagUser = "dagX";
        String result = ApplicationSubmissionHelper.getProxyUser(user, dagUser);
        assertEquals(dagUser, result, "If user matches another entry in AIRFLOW_SYSTEM_ACCOUNTS, should return dagUser");
    }

    @Test
    public void testGetProxyUser_whenUserIsNotInAirflowAccounts_returnsUser() {
        String user = "regularUser";
        String dagUser = "dagOwner";
        String result = ApplicationSubmissionHelper.getProxyUser(user, dagUser);
        assertEquals(user, result, "If user is not an airflow system account, should return the original user");
    }

    @Test
    public void testGetProxyUser_caseSensitivity_behavior() {
        // The set we inserted is lowercase; check that a different case does not match (case-sensitive expected)
        String user = "Airflow";
        String dagUser = "dagCase";
        String result = ApplicationSubmissionHelper.getProxyUser(user, dagUser);
        assertEquals(user, result, "Contains should be case-sensitive; different case should not match and should return user");
    }

    @Test
    public void testGetProxyUser_viaReflection_invocation() throws Exception {
        Method method = ApplicationSubmissionHelper.class.getDeclaredMethod("getProxyUser", String.class, String.class);
        // even though public, invoke via reflection to validate reflective access
        method.setAccessible(true);
        Object out = method.invoke(null, "airflow-system", "reflectedDag");
        assertTrue(out instanceof String);
        assertEquals("reflectedDag", out);
    }
}
