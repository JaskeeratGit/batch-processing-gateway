package com.apple.spark.core;

import com.apple.spark.AppConfig;
import com.apple.spark.operator.SparkApplication;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.lang.reflect.Field;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for RunningApplicationMonitor.onUpdate(...)
 *
 * This test verifies that when the SparkApplication is in RUNNING state but its metadata.name is null,
 * the monitor does not add an entry into its runningApplications map.
 */
public class RunningApplicationMonitor_onUpdate_1_0_Test_testOnUpdate_nameNull_logsAndDoesNothing {

    private Timer timer;
    private SimpleMeterRegistry meterRegistry;
    private AppConfig.SparkCluster sparkClusterMock;
    private RunningApplicationMonitor monitor;

    @BeforeEach
    public void setup() {
        timer = new Timer(true);
        meterRegistry = new SimpleMeterRegistry();
        sparkClusterMock = Mockito.mock(AppConfig.SparkCluster.class);
        // return empty strings for tags to avoid null handling in constructor logging/registration
        Mockito.when(sparkClusterMock.getEksCluster()).thenReturn("");
        Mockito.when(sparkClusterMock.getSparkApplicationNamespace()).thenReturn("");
        // Use a large delete interval to avoid TimerTask triggering during tests
        monitor = new RunningApplicationMonitor(sparkClusterMock, timer, 60 * 60 * 1000L, meterRegistry);
    }

    @AfterEach
    public void teardown() {
        timer.cancel();
    }

    @SuppressWarnings("unchecked")
    private ConcurrentHashMap<?, ?> getRunningApplicationsMap() throws Exception {
        Field f = RunningApplicationMonitor.class.getDeclaredField("runningApplications");
        f.setAccessible(true);
        return (ConcurrentHashMap<?, ?>) f.get(monitor);
    }

    @Test
    public void testOnUpdate_nameNull_logsAndDoesNothing() throws Exception {
        // Create a deep-stubbed SparkApplication so we can stub nested calls like getStatus().getApplicationState().getState()
        SparkApplication currDeep = Mockito.mock(SparkApplication.class, Mockito.RETURNS_DEEP_STUBS);
        // Stub nested state to "RUNNING"
        Mockito.when(currDeep.getStatus().getApplicationState().getState()).thenReturn("RUNNING");

        // metadata mock with null name
        ObjectMeta meta = Mockito.mock(ObjectMeta.class);
        Mockito.when(currDeep.getMetadata()).thenReturn(meta);
        Mockito.when(meta.getName()).thenReturn(null);

        // Invoke the method under test
        monitor.onUpdate(null, currDeep);

        // Verify runningApplications map is unchanged (no entry added)
        ConcurrentHashMap<?, ?> map = getRunningApplicationsMap();
        assertEquals(0, map.size());
    }
}
