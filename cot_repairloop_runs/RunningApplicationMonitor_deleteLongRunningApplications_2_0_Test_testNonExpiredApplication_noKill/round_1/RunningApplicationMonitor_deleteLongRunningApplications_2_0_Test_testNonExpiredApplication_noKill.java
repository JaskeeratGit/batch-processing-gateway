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

        // Pass the provided sparkCluster to super so RunningApplicationMonitor ctor can use it.
        TestRunningApplicationMonitor(AppConfig.SparkCluster sparkCluster, Timer timer) {
            super(sparkCluster, timer, new SimpleMeterRegistry());
        }

        TestRunningApplicationMonitor(AppConfig.SparkCluster sparkCluster, Timer timer, long deleteIntervalMillis) {
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
        // Create a real SparkCluster instance to be passed to the RunningApplicationMonitor ctor.
        AppConfig.SparkCluster sparkCluster = new AppConfig.SparkCluster();
        sparkCluster.setEksCluster("eks3");
        sparkCluster.setSparkApplicationNamespace("ns3");

        TestRunningApplicationMonitor monitor = new TestRunningApplicationMonitor(sparkCluster, new NoopTimer());

        Field runningAppsField = RunningApplicationMonitor.class.getDeclaredField("runningApplications");
        runningAppsField.setAccessible(true);

        @SuppressWarnings("unchecked")
        ConcurrentHashMap<com.apple.spark.core.NamespaceAndName, com.apple.spark.core.RunningApplicationMonitor.RunningAppInfo> map =
            new ConcurrentHashMap<>();

        // Not expired: large maxRunningMillis
        // Create a RunningAppInfo instance compatible with the RunningApplicationMonitor's inner type if possible.
        // If the production RunningAppInfo class is present, use reflection to create it; otherwise use the minimal nested class below.
        Object notExpired;
        try {
            // Try to instantiate the production RunningApplicationMonitor.RunningAppInfo via reflection
            Class<?> runningAppInfoClass = Class.forName("com.apple.spark.core.RunningApplicationMonitor$RunningAppInfo");
            try {
                // prefer a constructor (long creationTimeMillis, long maxRunningMillis)
                notExpired = runningAppInfoClass.getConstructor(long.class, long.class)
                    .newInstance(System.currentTimeMillis(), 10_000_000L);
            } catch (NoSuchMethodException e) {
                // fallback: try zero-arg + setters (unlikely), but fall back to our minimal class below
                notExpired = new RunningAppInfo(System.currentTimeMillis(), 10_000_000L);
            }
        } catch (ClassNotFoundException e) {
            // production class not present; use minimal nested RunningAppInfo below
            notExpired = new RunningAppInfo(System.currentTimeMillis(), 10_000_000L);
        }

        // Create a key. Prefer production NamespaceAndName if present, otherwise use minimal nested one.
        Object keyObj;
        try {
            Class<?> nsNameClass = Class.forName("com.apple.spark.core.NamespaceAndName");
            try {
                keyObj = nsNameClass.getConstructor(String.class, String.class).newInstance("ns", "not-expired-app");
            } catch (Exception ex) {
                // fallback to nested class
                keyObj = new NamespaceAndName("ns", "not-expired-app");
            }
        } catch (ClassNotFoundException ex) {
            keyObj = new NamespaceAndName("ns", "not-expired-app");
        }

        // Put into the raw ConcurrentHashMap (avoid generic type issues via raw reference)
        @SuppressWarnings("unchecked")
        ConcurrentHashMap rawMap = map;
        rawMap.put(keyObj, notExpired);

        runningAppsField.set(monitor, rawMap);

        monitor.deleteLongRunningApplications();

        // No kill should be invoked
        assertTrue(monitor.killedApps.isEmpty());

        // Map still contains entry
        assertTrue(rawMap.containsKey(keyObj));
    }

    // --- Minimal helper / fallback classes (used only if test environment lacks real ones) ---
    // Minimal NamespaceAndName (matches signature provided)
    public static class NamespaceAndName {

        private String namespace;

        private String name;

        public NamespaceAndName() {
        }

        public NamespaceAndName(String namespace, String name) {
            this.namespace = namespace;
            this.name = name;
        }

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof NamespaceAndName))
                return false;
            NamespaceAndName that = (NamespaceAndName) o;
            return (namespace == null ? that.namespace == null : namespace.equals(that.namespace)) && (name == null ? that.name == null : name.equals(that.name));
        }

        @Override
        public int hashCode() {
            int result = namespace != null ? namespace.hashCode() : 0;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            return result;
        }
    }

    // Minimal RunningAppInfo to support exceedMaxRunningTime() and getCreationTimeMillis()
    public static class RunningAppInfo {

        private final long creationTimeMillis;

        private final long maxRunningMillis;

        public RunningAppInfo(long creationTimeMillis, long maxRunningMillis) {
            this.creationTimeMillis = creationTimeMillis;
            this.maxRunningMillis = maxRunningMillis;
        }

        public boolean exceedMaxRunningTime() {
            return System.currentTimeMillis() - creationTimeMillis > maxRunningMillis;
        }

        public long getCreationTimeMillis() {
            return creationTimeMillis;
        }
    }

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

    // Minimal CounterMetricContainer stub (no-op)
    public static class CounterMetricContainer {

        public CounterMetricContainer(io.micrometer.core.instrument.MeterRegistry r) {
        }

        public void increment(String metricName, io.micrometer.core.instrument.Tag... tags) {
            // no-op
        }
    }

    // Minimal GaugeMetricContainer stub (no-op register)
    public static class GaugeMetricContainer {

        public GaugeMetricContainer(io.micrometer.core.instrument.MeterRegistry r) {
        }

        public void register(String name, java.util.function.Supplier<Number> supplier, io.micrometer.core.instrument.Tag... tags) {
            // no-op
        }
    }

    // Minimal Constants used by RunningApplicationMonitor
    public static class Constants {

        public static final String MONITOR_RUNNING_APPS = "monitor.running.apps";

        public static final String MONITOR_KILLED_APPS = "monitor.killed.apps";

        public static final String QUEUE_LABEL = "spark-app-queue";
    }
}
