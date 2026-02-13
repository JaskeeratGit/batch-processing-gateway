package com.apple.spark.core;

import com.apple.spark.AppConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RunningApplicationMonitor.deleteLongRunningApplications()
 */
public class RunningApplicationMonitor_deleteLongRunningApplications_2_0_Test_testDeleteExpiredApplication_removedByOtherThread_noKill {

    private SimpleMeterRegistry meterRegistry;

    @BeforeEach
    public void setUp() {
        meterRegistry = new SimpleMeterRegistry();
    }

    // A Timer that does not schedule anything (prevents background tasks)
    private static class NoopTimer extends Timer {

        @Override
        public void schedule(TimerTask task, long delay, long period) {
            // no-op to avoid starting background threads in unit tests
        }
    }

    // Subclass RunningApplicationMonitor to override killApplication to capture calls
    private static class TestRunningApplicationMonitor extends RunningApplicationMonitor {

        final List<String> killedApps = new ArrayList<>();

        TestRunningApplicationMonitor(com.apple.spark.AppConfig.SparkCluster sparkCluster, Timer timer) {
            super(sparkCluster, timer, new SimpleMeterRegistry());
        }

        TestRunningApplicationMonitor(com.apple.spark.AppConfig.SparkCluster sparkCluster, Timer timer, long deleteIntervalMillis) {
            super(sparkCluster, timer, deleteIntervalMillis, new SimpleMeterRegistry());
        }

        @Override
        protected void killApplication(String namespace, String appName) {
            // record kill attempts instead of performing real Kubernetes operations
            killedApps.add(namespace + "/" + appName);
        }
    }


    @Test
    public void testDeleteExpiredApplication_removedByOtherThread_noKill() throws Exception {
        com.apple.spark.AppConfig.SparkCluster sparkCluster = new com.apple.spark.AppConfig.SparkCluster();
        sparkCluster.setEksCluster("eks2");
        sparkCluster.setSparkApplicationNamespace("ns2");
        TestRunningApplicationMonitor monitor = new TestRunningApplicationMonitor(sparkCluster, new NoopTimer());
        // Create a map that appears to contain an expired entry but its remove method returns null
        class RemoveReturnsNullMap extends ConcurrentHashMap<NamespaceAndName, RunningApplicationMonitor.RunningAppInfo> {

            @Override
            public RunningApplicationMonitor.RunningAppInfo remove(Object key) {
                // simulate concurrent removal by another thread: return null (no value removed)
                return null;
            }
        }
        RemoveReturnsNullMap map = new RemoveReturnsNullMap();
        RunningApplicationMonitor.RunningAppInfo expired = new RunningApplicationMonitor.RunningAppInfo(System.currentTimeMillis() - 10_000L, 1L);
        NamespaceAndName key = new NamespaceAndName("concurrent-ns", "concurrent-app");
        map.put(key, expired);
        // Inject map
        Field runningAppsField = RunningApplicationMonitor.class.getDeclaredField("runningApplications");
        runningAppsField.setAccessible(true);
        runningAppsField.set(monitor, map);
        // Call the focal method
        monitor.deleteLongRunningApplications();
        // Because remove returned null, killApplication should not be invoked
        assertTrue(monitor.killedApps.isEmpty());
        // The map should still contain the entry (since remove returned null)
        assertTrue(map.containsKey(key));
    }
}
