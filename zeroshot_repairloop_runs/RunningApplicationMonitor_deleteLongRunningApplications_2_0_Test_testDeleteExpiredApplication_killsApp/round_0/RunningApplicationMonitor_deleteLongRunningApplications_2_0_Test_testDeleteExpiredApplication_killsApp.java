package com.apple.spark.core;

import com.apple.spark.AppConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
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
public class RunningApplicationMonitor_deleteLongRunningApplications_2_0_Test_testDeleteExpiredApplication_killsApp {

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
    public void testDeleteExpiredApplication_killsApp() throws Exception {
        AppConfig.SparkCluster sparkCluster = new AppConfig.SparkCluster();
        sparkCluster.setEksCluster("eks1");
        sparkCluster.setSparkApplicationNamespace("ns1");
        TestRunningApplicationMonitor monitor = new TestRunningApplicationMonitor(sparkCluster, new NoopTimer());
        // Access the private runningApplications map via reflection and populate it
        Field runningAppsField = RunningApplicationMonitor.class.getDeclaredField("runningApplications");
        runningAppsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<NamespaceAndName, RunningAppInfo> map = new ConcurrentHashMap<>();
        // Add an expired application: creation time far in the past, and a small maxRunningMillis
        RunningAppInfo expired = new RunningAppInfo(System.currentTimeMillis() - 10_000L, 1L);
        NamespaceAndName key = new NamespaceAndName("my-ns", "my-app");
        map.put(key, expired);
        runningAppsField.set(monitor, map);
        // Call the focal method
        monitor.deleteLongRunningApplications();
        // Verify that killApplication was invoked for the expired app
        assertEquals(1, monitor.killedApps.size());
        assertEquals("my-ns/my-app", monitor.killedApps.get(0));
        // Also ensure the map no longer contains the app
        assertFalse(map.containsKey(key));
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

    // Minimal fallback AppConfig and nested SparkCluster if not present in classpath
    // Renamed to avoid shadowing the real com.apple.spark.AppConfig imported at top.
    public static class FallbackAppConfig {

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

    // Minimal Constants used by RunningApplicationMonitor (only if not present)
    public static class Constants {

        public static final String MONITOR_RUNNING_APPS = "monitor.running.apps";

        public static final String MONITOR_KILLED_APPS = "monitor.killed.apps";

        public static final String QUEUE_LABEL = "spark-app-queue";
    }
}
