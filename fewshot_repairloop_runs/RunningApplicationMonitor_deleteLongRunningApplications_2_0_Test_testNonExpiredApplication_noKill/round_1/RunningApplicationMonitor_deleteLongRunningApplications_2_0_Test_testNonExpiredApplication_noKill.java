package com.apple.spark.core;

import com.apple.spark.AppConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.lang.reflect.Constructor;
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
 *
 * Note: This test file provides minimal helper classes (if they are not present in the test
 * environment) to allow the tests to compile and run in isolation. If the real production classes
 * exist in the classpath, those will be used instead.
 */
public class RunningApplicationMonitor_deleteLongRunningApplications_2_0_Test_testNonExpiredApplication_noKill {

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
    public void testNonExpiredApplication_noKill() throws Exception {
        com.apple.spark.AppConfig.SparkCluster sparkCluster = new com.apple.spark.AppConfig.SparkCluster();
        sparkCluster.setEksCluster("eks3");
        sparkCluster.setSparkApplicationNamespace("ns3");
        TestRunningApplicationMonitor monitor = new TestRunningApplicationMonitor(sparkCluster, new NoopTimer());

        Field runningAppsField = RunningApplicationMonitor.class.getDeclaredField("runningApplications");
        runningAppsField.setAccessible(true);

        // Obtain existing map (create one if null)
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<Object, Object> map = (ConcurrentHashMap<Object, Object>) runningAppsField.get(monitor);
        if (map == null) {
            map = new ConcurrentHashMap<>();
            runningAppsField.set(monitor, map);
        }

        // Create an instance of the real RunningApplicationMonitor.RunningAppInfo via reflection
        Class<?> runningAppInfoClass = null;
        for (Class<?> c : RunningApplicationMonitor.class.getDeclaredClasses()) {
            if ("RunningAppInfo".equals(c.getSimpleName())) {
                runningAppInfoClass = c;
                break;
            }
        }
        if (runningAppInfoClass == null) {
            throw new IllegalStateException("RunningAppInfo inner class not found on RunningApplicationMonitor");
        }

        long creation = System.currentTimeMillis();
        long maxRunning = 10_000_000L;

        Object runningAppInfoInstance;
        try {
            Constructor<?> ctor = runningAppInfoClass.getDeclaredConstructor(long.class, long.class);
            ctor.setAccessible(true);
            runningAppInfoInstance = ctor.newInstance(creation, maxRunning);
        } catch (NoSuchMethodException e) {
            // Maybe it's a non-static inner class with an outer instance parameter
            Constructor<?> ctor = runningAppInfoClass.getDeclaredConstructor(RunningApplicationMonitor.class, long.class, long.class);
            ctor.setAccessible(true);
            runningAppInfoInstance = ctor.newInstance(monitor, creation, maxRunning);
        }

        // Create a NamespaceAndName instance using the top-level class in the same package
        Class<?> namespaceAndNameClass = Class.forName("com.apple.spark.core.NamespaceAndName");
        Object keyInstance;
        try {
            Constructor<?> keyCtor = namespaceAndNameClass.getDeclaredConstructor(String.class, String.class);
            keyCtor.setAccessible(true);
            keyInstance = keyCtor.newInstance("ns", "not-expired-app");
        } catch (NoSuchMethodException ex) {
            // fallback to no-arg + setters
            Object tmp = namespaceAndNameClass.newInstance();
            namespaceAndNameClass.getMethod("setNamespace", String.class).invoke(tmp, "ns");
            namespaceAndNameClass.getMethod("setName", String.class).invoke(tmp, "not-expired-app");
            keyInstance = tmp;
        }

        // Put into the existing map (uses the correct runtime types)
        map.put(keyInstance, runningAppInfoInstance);

        monitor.deleteLongRunningApplications();

        // No kill should be invoked
        assertTrue(monitor.killedApps.isEmpty());
        // Map still contains entry
        assertTrue(map.containsKey(keyInstance));
    }

    // --- Minimal helper / fallback classes (used only if test environment lacks real ones) ---
    // These are local to the test class to avoid interfering with production top-level classes.

    // Minimal AppConfig and nested SparkCluster if not present in classpath
    public static class AppConfig {

        public static class SparkCluster {

            private String eksCluster;

            private String sparkApplicationNamespace;

            public String getEksCluster() {
                return eksCluster;
            }

            public void setEksCluster(String eksCluster) {
                this.eksCluster = eksCluster;
            }

            public String getSparkApplicationNamespace() {
                return sparkApplicationNamespace;
            }

            public void setSparkApplicationNamespace(String sparkApplicationNamespace) {
                this.sparkApplicationNamespace = sparkApplicationNamespace;
            }
        }
    }
}
