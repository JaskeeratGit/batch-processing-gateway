package com.apple.spark.core;

import com.apple.spark.AppConfig;
import com.apple.spark.api.SubmitApplicationRequest;
import java.lang.reflect.Method;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static com.apple.spark.core.Constants.*;
import com.apple.spark.operator.*;
import com.google.common.annotations.VisibleForTesting;
import java.util.*;

/**
 * Unit tests for SparkPodNodeAffinityHelper.createNodeAffinityForSparkPods(...)
 *
 * These tests use reflection to invoke the private helper createNodeSelectorTermForRequired(...)
 * to determine expected behavior, then invoke the public focal method and assert consistency.
 */
public class SparkPodNodeAffinityHelper_createNodeAffinityForSparkPods_0_0_Test_testCreateNodeAffinity_driverFlagFalse_consistentWithSelectorTerm {

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


    @Test
    public void testCreateNodeAffinity_driverFlagFalse_consistentWithSelectorTerm() throws Exception {
        SubmitApplicationRequest req = new SubmitApplicationRequest();
        AppConfig appConfig = new AppConfig();
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
