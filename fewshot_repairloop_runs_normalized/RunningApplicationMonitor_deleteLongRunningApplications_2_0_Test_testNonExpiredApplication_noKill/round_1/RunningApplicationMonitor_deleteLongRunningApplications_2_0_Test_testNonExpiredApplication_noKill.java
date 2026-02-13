package com.apple.spark.core;

import com.apple.spark.AppConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static com.apple.spark.core.Constants.MONITOR_KILLED_APPS;
import static com.apple.spark.core.Constants.MONITOR_RUNNING_APPS;
import com.apple.spark.operator.DriverInfo;
import com.apple.spark.operator.SparkApplication;
import com.apple.spark.operator.SparkApplicationResourceList;
import com.apple.spark.util.CounterMetricContainer;
import com.apple.spark.util.DateTimeUtils;
import com.apple.spark.util.GaugeMetricContainer;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        // Build a map using the production NamespaceAndName and RunningAppInfo types to avoid ClassCast issues.
        ConcurrentHashMap<Object, Object> map = new ConcurrentHashMap<>();
        // Not expired: large maxRunningMillis

        // Create production NamespaceAndName instance
        Class<?> prodNamespaceClass = Class.forName("com.apple.spark.core.NamespaceAndName");
        Object key = prodNamespaceClass.getConstructor(String.class, String.class).newInstance("ns", "not-expired-app");

        // Create production RunningAppInfo instance (nested class inside RunningApplicationMonitor)
        Class<?> prodRunningAppInfoClass = null;
        for (Class<?> c : RunningApplicationMonitor.class.getDeclaredClasses()) {
            if ("RunningAppInfo".equals(c.getSimpleName())) {
                prodRunningAppInfoClass = c;
                break;
            }
        }
        if (prodRunningAppInfoClass == null) {
            throw new IllegalStateException("Could not find RunningAppInfo nested class in RunningApplicationMonitor");
        }
        Constructor<?> raiCtor = null;
        try {
            raiCtor = prodRunningAppInfoClass.getDeclaredConstructor(long.class, long.class);
        } catch (NoSuchMethodException e) {
            // try Long.class types as fallback (unlikely)
            raiCtor = prodRunningAppInfoClass.getDeclaredConstructor(Long.class, Long.class);
        }
        raiCtor.setAccessible(true);
        Object notExpired = raiCtor.newInstance(System.currentTimeMillis(), 10_000_000L);

        map.put(key, notExpired);
        runningAppsField.set(monitor, map);
        monitor.deleteLongRunningApplications();
        // No kill should be invoked
        assertTrue(monitor.killedApps.isEmpty());
        // Map still contains entry
        // Need to verify using production key equals; use containsKey with the same key instance
        assertTrue(map.containsKey(key));
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
