package com.apple.spark.core;

import com.apple.spark.AppConfig;
import com.apple.spark.api.SubmitApplicationRequest;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.google.common.annotations.VisibleForTesting;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static com.apple.spark.core.Constants.*;
import com.apple.spark.operator.*;
import java.util.*;

/**
 * Unit tests for SparkPodNodeAffinityHelper.createNodeAffinityForSparkPods(...)
 *
 * These tests use reflection to invoke the private helper createNodeSelectorTermForRequired(...)
 * to determine expected behavior, then invoke the public focal method and assert consistency.
 */
public class SparkPodNodeAffinityHelper_createNodeAffinityForSparkPods_0_0_Test {

    /**
     * Helper that invokes the private static method
     * createNodeSelectorTermForRequired(SubmitApplicationRequest, AppConfig, String, AppConfig.SparkCluster, boolean)
     * via reflection and returns the NodeSelectorTerm.
     */
    private NodeSelectorTerm invokeCreateNodeSelectorTermForRequired(SubmitApplicationRequest request, AppConfig appConfig, String queueName, AppConfig.SparkCluster sparkCluster, boolean isDriver) throws Exception {
        Method m = SparkPodNodeAffinityHelper.class.getDeclaredMethod("createNodeSelectorTermForRequired", SubmitApplicationRequest.class, AppConfig.class, String.class, AppConfig.SparkCluster.class, boolean.class);
        m.setAccessible(true);
        return (NodeSelectorTerm) m.invoke(null, request, appConfig, queueName, sparkCluster, isDriver);
    }

    /**
     * Ensure AppConfig.getQueues() will not return null to avoid NPE in the code under test.
     * Tries to call setQueues(List) if available; otherwise sets a 'queues' field reflectively.
     */
    private void ensureQueuesNonNull(AppConfig appConfig) throws Exception {
        // Try setter first
        try {
            Method setQueues = AppConfig.class.getMethod("setQueues", List.class);
            setQueues.invoke(appConfig, new ArrayList<>());
            return;
        } catch (NoSuchMethodException ignored) {
            // fallback to field set
        }
        // Try to set a declared field named 'queues'
        try {
            Field queuesField = AppConfig.class.getDeclaredField("queues");
            queuesField.setAccessible(true);
            queuesField.set(appConfig, new ArrayList<>());
            return;
        } catch (NoSuchFieldException ignored) {
            // If field not found, try some common alternatives
        }
        // Try alternative common field name
        try {
            Field queuesField = AppConfig.class.getDeclaredField("queueConfigs");
            queuesField.setAccessible(true);
            queuesField.set(appConfig, new ArrayList<>());
            return;
        } catch (NoSuchFieldException ignored) {
            // give up and throw a clear exception
        }
        throw new IllegalStateException("Unable to initialize queues on AppConfig via reflection. Neither setQueues(List) nor expected fields were found.");
    }

    @Test
    public void testCreateNodeAffinity_driverFlagTrue_consistentWithSelectorTerm() throws Exception {
        SubmitApplicationRequest req = new SubmitApplicationRequest();
        AppConfig appConfig = new AppConfig();
        ensureQueuesNonNull(appConfig);
        String queueName = "default";
        // use null cluster for this scenario
        AppConfig.SparkCluster sparkCluster = null;
        boolean isDriver = true;
        NodeSelectorTerm selectorTerm = invokeCreateNodeSelectorTermForRequired(req, appConfig, queueName, sparkCluster, isDriver);
        NodeAffinity affinity = SparkPodNodeAffinityHelper.createNodeAffinityForSparkPods(req, appConfig, queueName, sparkCluster, isDriver);
        if (selectorTerm == null || selectorTerm.getMatchExpressions() == null) {
            assertNull(affinity.getRequiredDuringSchedulingIgnoredDuringExecution(), "When selector term has no match expressions, NodeAffinity.requiredDuringSchedulingIgnoredDuringExecution should be null");
        } else {
            assertNotNull(affinity.getRequiredDuringSchedulingIgnoredDuringExecution(), "When selector term has match expressions, NodeAffinity.requiredDuringSchedulingIgnoredDuringExecution should be set");
        }
    }

    @Test
    public void testCreateNodeAffinity_driverFlagFalse_consistentWithSelectorTerm() throws Exception {
        SubmitApplicationRequest req = new SubmitApplicationRequest();
        AppConfig appConfig = new AppConfig();
        ensureQueuesNonNull(appConfig);
        String queueName = "custom-queue";
        // keep null to vary only queueName
        AppConfig.SparkCluster sparkCluster = null;
        boolean isDriver = false;
        NodeSelectorTerm selectorTerm = invokeCreateNodeSelectorTermForRequired(req, appConfig, queueName, sparkCluster, isDriver);
        NodeAffinity affinity = SparkPodNodeAffinityHelper.createNodeAffinityForSparkPods(req, appConfig, queueName, sparkCluster, isDriver);
        if (selectorTerm == null || selectorTerm.getMatchExpressions() == null) {
            assertNull(affinity.getRequiredDuringSchedulingIgnoredDuringExecution(), "When selector term has no match expressions, NodeAffinity.requiredDuringSchedulingIgnoredDuringExecution should be null");
        } else {
            assertNotNull(affinity.getRequiredDuringSchedulingIgnoredDuringExecution(), "When selector term has match expressions, NodeAffinity.requiredDuringSchedulingIgnoredDuringExecution should be set");
        }
    }
}
