package com.apple.spark.core;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RunningApplicationMonitor.deleteLongRunningApplications()
 *
 * This test uses the real production AppConfig (com.apple.spark.AppConfig) and the real
 * com.apple.spark.core.NamespaceAndName / RunningApplicationMonitor.RunningAppInfo types when
 * available on the test classpath. Mockito is used to create a RunningAppInfo test double so we
 * can control exceedMaxRunningTime() and creation time without depending on production internals.
 */
public class RunningApplicationMonitor_deleteLongRunningApplications_2_0_Test_testDeleteExpiredApplication_killsApp {

    private SimpleMeterRegistry meterRegistry;

    @BeforeEach
    public void setUp() {
        meterRegistry = new SimpleMeterRegistry();
    }

    // A Timer that does not schedule anything (prevents background tasks)
    private static class NoopTimer extends Timer {
        // use daemon timer thread so tests don't hang on JVM shutdown if any thread is created
        NoopTimer() {
            super(true);
        }

        @Override
        public void schedule(TimerTask task, long delay, long period) {
            // no-op to avoid starting background threads in unit tests
        }

        @Override
        public void schedule(TimerTask task, long delay) {
            // no-op
        }
    }

    // Subclass RunningApplicationMonitor to override killApplication to capture calls
    private static class TestRunningApplicationMonitor extends RunningApplicationMonitor {

        final List<String> killedApps = new ArrayList<>();

        // Use the production com.apple.spark.AppConfig.SparkCluster type explicitly to avoid
        // accidentally referencing a local helper AppConfig that may be present in some test setups.
        TestRunningApplicationMonitor(Timer timer) {
            super(new com.apple.spark.AppConfig.SparkCluster(), timer, new SimpleMeterRegistry());
        }

        TestRunningApplicationMonitor(Timer timer, long deleteIntervalMillis) {
            super(new com.apple.spark.AppConfig.SparkCluster(), timer, deleteIntervalMillis, new SimpleMeterRegistry());
        }

        @Override
        protected void killApplication(String namespace, String appName) {
            // record kill attempts instead of performing real Kubernetes operations
            killedApps.add(namespace + "/" + appName);
        }
    }

    @Test
    public void testDeleteExpiredApplication_killsApp() throws Exception {
        TestRunningApplicationMonitor monitor = new TestRunningApplicationMonitor(new NoopTimer());
        // Access the private runningApplications map via reflection and populate it
        Field runningAppsField = RunningApplicationMonitor.class.getDeclaredField("runningApplications");
        runningAppsField.setAccessible(true);

        // Use the production NamespaceAndName and RunningAppInfo types (they are in the same package).
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<NamespaceAndName, RunningApplicationMonitor.RunningAppInfo> map =
                new ConcurrentHashMap<>();

        // Create a mock RunningAppInfo so we can force it to be "expired"
        RunningApplicationMonitor.RunningAppInfo expired =
                Mockito.mock(RunningApplicationMonitor.RunningAppInfo.class);
        Mockito.when(expired.exceedMaxRunningTime()).thenReturn(true);
        // provide a creation time in the past for log computation (not strictly required)
        Mockito.when(expired.getCreationTimeMillis()).thenReturn(System.currentTimeMillis() - 10_000L);

        NamespaceAndName key = new NamespaceAndName("my-ns", "my-app");
        map.put(key, expired);

        // Inject the map into the monitor instance
        runningAppsField.set(monitor, map);

        // Call the focal method
        monitor.deleteLongRunningApplications();

        // Verify that killApplication was invoked for the expired app
        assertEquals(1, monitor.killedApps.size());
        assertEquals("my-ns/my-app", monitor.killedApps.get(0));
        // Also ensure the map no longer contains the app
        assertFalse(map.containsKey(key));
    }
}
