package com.apple.spark.core;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static com.apple.spark.core.BatchSchedulerConstants.PLACEHOLDER_TIMEOUT_IN_SECONDS;
import static com.apple.spark.core.BatchSchedulerConstants.YUNIKORN_ROOT_QUEUE;
import static com.apple.spark.core.BatchSchedulerConstants.YUNIKORN_SPARK_DEFAULT_QUEUE;
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
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static com.apple.spark.core.Constants.*;

/**
 * JUnit5 tests for ApplicationSubmissionHelper.getProxyUser(...)
 */
public class ApplicationSubmissionHelper_getProxyUser_1_0_Test {

    private Field airflowField;

    private Object originalAirflowValue;

    private Field modifiersField;

    private boolean replacedFieldReference = false;

    @BeforeEach
    public void setUp() throws Exception {
        // Obtain the Constants class and the AIRFLOW_SYSTEM_ACCOUNTS field via reflection.
        Class<?> constantsClass = Class.forName("com.apple.spark.core.Constants");
        airflowField = constantsClass.getDeclaredField("AIRFLOW_SYSTEM_ACCOUNTS");
        airflowField.setAccessible(true);
        // Save original value to restore later
        originalAirflowValue = airflowField.get(null);
        // Replace with a predictable set for testing
        Set<String> testSet = new HashSet<>(Arrays.asList("airflow-system", "airflow"));
        // Try to mutate the existing Set instance if possible.
        if (originalAirflowValue instanceof Set) {
            @SuppressWarnings("unchecked")
            Set<Object> originalSet = (Set<Object>) originalAirflowValue;
            try {
                // Try to clear and addAll - works if the set is mutable.
                originalSet.clear();
                originalSet.addAll(testSet);
                replacedFieldReference = false;
                return;
            } catch (UnsupportedOperationException e) {
                // Not mutable - fall through to attempt replacing the field reference.
            }
        }
        // Try to remove final modifier and set the field value.
        try {
            modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(airflowField, airflowField.getModifiers() & ~Modifier.FINAL);
            airflowField.set(null, testSet);
            replacedFieldReference = true;
            return;
        } catch (NoSuchFieldException | IllegalAccessException | SecurityException ignore) {
            // Fall through to VarHandle approach for newer JVMs.
        }
        // Try VarHandle (Java 9+) to set the static field even if it's final.
        try {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(constantsClass, MethodHandles.lookup());
            java.lang.invoke.VarHandle vh = lookup.findStaticVarHandle(constantsClass, airflowField.getName(), Set.class);
            vh.set(testSet);
            replacedFieldReference = true;
            return;
        } catch (Throwable t) {
            // If we reach here, we couldn't change the field reference.
            throw new IllegalStateException("Failed to set AIRFLOW_SYSTEM_ACCOUNTS for tests", t);
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (airflowField == null) {
            return;
        }
        // If we replaced the field reference, attempt to put it back.
        if (replacedFieldReference) {
            try {
                // Try to set via direct Field.set
                airflowField.set(null, originalAirflowValue);
                return;
            } catch (IllegalAccessException ignore) {
                // Try with modifiersField if available
                try {
                    if (modifiersField == null) {
                        modifiersField = Field.class.getDeclaredField("modifiers");
                        modifiersField.setAccessible(true);
                    }
                    modifiersField.setInt(airflowField, airflowField.getModifiers() & ~Modifier.FINAL);
                    airflowField.set(null, originalAirflowValue);
                    return;
                } catch (Throwable ignore2) {
                    // Try VarHandle approach
                    try {
                        Class<?> constantsClass = Class.forName("com.apple.spark.core.Constants");
                        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(constantsClass, MethodHandles.lookup());
                        java.lang.invoke.VarHandle vh = lookup.findStaticVarHandle(constantsClass, airflowField.getName(), Set.class);
                        vh.set(originalAirflowValue);
                        return;
                    } catch (Throwable t) {
                        throw new IllegalStateException("Failed to restore AIRFLOW_SYSTEM_ACCOUNTS after tests", t);
                    }
                }
            }
        } else {
            // We mutated the original set instance; try to restore its previous contents.
            if (originalAirflowValue instanceof Set) {
                Object current = airflowField.get(null);
                if (current instanceof Set) {
                    @SuppressWarnings("unchecked")
                    Set<Object> currentSet = (Set<Object>) current;
                    @SuppressWarnings("unchecked")
                    Set<Object> originalSet = (Set<Object>) originalAirflowValue;
                    try {
                        currentSet.clear();
                        currentSet.addAll(originalSet);
                        return;
                    } catch (UnsupportedOperationException ignore) {
                        // If we can't mutate the current set, try to replace the field reference.
                        try {
                            if (modifiersField == null) {
                                modifiersField = Field.class.getDeclaredField("modifiers");
                                modifiersField.setAccessible(true);
                            }
                            modifiersField.setInt(airflowField, airflowField.getModifiers() & ~Modifier.FINAL);
                            airflowField.set(null, originalAirflowValue);
                            return;
                        } catch (Throwable t) {
                            // Try VarHandle as last resort
                            try {
                                Class<?> constantsClass = Class.forName("com.apple.spark.core.Constants");
                                MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(constantsClass, MethodHandles.lookup());
                                java.lang.invoke.VarHandle vh = lookup.findStaticVarHandle(constantsClass, airflowField.getName(), Set.class);
                                vh.set(originalAirflowValue);
                                return;
                            } catch (Throwable tt) {
                                throw new IllegalStateException("Failed to restore AIRFLOW_SYSTEM_ACCOUNTS after tests", tt);
                            }
                        }
                    }
                } else {
                    // current is not a Set instance, attempt to set back directly
                    try {
                        if (modifiersField == null) {
                            modifiersField = Field.class.getDeclaredField("modifiers");
                            modifiersField.setAccessible(true);
                        }
                        modifiersField.setInt(airflowField, airflowField.getModifiers() & ~Modifier.FINAL);
                        airflowField.set(null, originalAirflowValue);
                        return;
                    } catch (Throwable t) {
                        try {
                            Class<?> constantsClass = Class.forName("com.apple.spark.core.Constants");
                            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(constantsClass, MethodHandles.lookup());
                            java.lang.invoke.VarHandle vh = lookup.findStaticVarHandle(constantsClass, airflowField.getName(), Set.class);
                            vh.set(originalAirflowValue);
                            return;
                        } catch (Throwable tt) {
                            throw new IllegalStateException("Failed to restore AIRFLOW_SYSTEM_ACCOUNTS after tests", tt);
                        }
                    }
                }
            } else {
                // original wasn't a Set; attempt to set back directly
                try {
                    if (modifiersField == null) {
                        modifiersField = Field.class.getDeclaredField("modifiers");
                        modifiersField.setAccessible(true);
                    }
                    modifiersField.setInt(airflowField, airflowField.getModifiers() & ~Modifier.FINAL);
                    airflowField.set(null, originalAirflowValue);
                } catch (Throwable t) {
                    try {
                        Class<?> constantsClass = Class.forName("com.apple.spark.core.Constants");
                        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(constantsClass, MethodHandles.lookup());
                        java.lang.invoke.VarHandle vh = lookup.findStaticVarHandle(constantsClass, airflowField.getName(), Set.class);
                        vh.set(originalAirflowValue);
                    } catch (Throwable tt) {
                        throw new IllegalStateException("Failed to restore AIRFLOW_SYSTEM_ACCOUNTS after tests", tt);
                    }
                }
            }
        }
    }

    @Test
    public void testGetProxyUser_viaReflection_invocation() throws Exception {
        Method m = ApplicationSubmissionHelper.class.getDeclaredMethod("getProxyUser", String.class, String.class);
        m.setAccessible(true);
        String result = (String) m.invoke(null, "airflow", "dagUser");
        assertEquals("dagUser", result);
    }

    @Test
    public void testGetProxyUser_whenUserIsInAirflowAccounts_returnsDagUser() {
        String result = ApplicationSubmissionHelper.getProxyUser("airflow", "dagUser");
        assertEquals("dagUser", result);
    }

    @Test
    public void testGetProxyUser_whenUserIsNotInAirflowAccounts_returnsUser() {
        String result = ApplicationSubmissionHelper.getProxyUser("someuser", "dagUser");
        assertEquals("someuser", result);
    }

    @Test
    public void testGetProxyUser_caseSensitivity_behavior() {
        // "Airflow" (capital A) is not in the test set which contains "airflow"
        String result = ApplicationSubmissionHelper.getProxyUser("Airflow", "dagUser");
        assertEquals("Airflow", result);
    }

    @Test
    public void testGetProxyUser_whenUserIsInAirflowAccountsDifferentEntry_returnsDagUser() {
        String result = ApplicationSubmissionHelper.getProxyUser("airflow-system", "dagUser");
        assertEquals("dagUser", result);
    }
}
