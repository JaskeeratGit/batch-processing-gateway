package com.apple.spark.core;

import com.apple.spark.AppConfig;
import com.apple.spark.operator.SparkApplication;
import com.apple.spark.core.SparkApplicationResourceHelper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import java.time.Instant;
import java.util.Timer;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static com.apple.spark.core.Constants.MONITOR_KILLED_APPS;
import static com.apple.spark.core.Constants.MONITOR_RUNNING_APPS;
import com.apple.spark.operator.DriverInfo;
import com.apple.spark.operator.SparkApplicationResourceList;
import com.apple.spark.util.CounterMetricContainer;
import com.apple.spark.util.DateTimeUtils;
import com.apple.spark.util.GaugeMetricContainer;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunningApplicationMonitor_onUpdate_1_1_Test {

    private Timer timer;

    @AfterEach
    public void tearDown() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    @Test
    public void testOnUpdate_nonRunningState_doesNothing() {
        timer = new Timer(true);
        AppConfig.SparkCluster sparkCluster = Mockito.mock(AppConfig.SparkCluster.class);
        Mockito.when(sparkCluster.getEksCluster()).thenReturn("eks");
        Mockito.when(sparkCluster.getSparkApplicationNamespace()).thenReturn("ns");
        RunningApplicationMonitor monitor = new RunningApplicationMonitor(sparkCluster, timer, new SimpleMeterRegistry());
        SparkApplication curr = new SparkApplication();
        ObjectMeta meta = new ObjectMeta();
        meta.setName("app-nonrunning");
        meta.setCreationTimestamp(Instant.now().toString());
        meta.setNamespace("ns");
        curr.setMetadata(meta);
        try (MockedStatic<SparkApplicationResourceHelper> mocked = Mockito.mockStatic(SparkApplicationResourceHelper.class)) {
            mocked.when(() -> SparkApplicationResourceHelper.getState(Mockito.any())).thenReturn("PENDING");
            monitor.onUpdate(null, curr);
            assertEquals(0, monitor.getApplicationCount());
        }
    }

    @Test
    public void testOnUpdate_runningNameNull_logsAndSkips() {
        timer = new Timer(true);
        AppConfig.SparkCluster sparkCluster = Mockito.mock(AppConfig.SparkCluster.class);
        Mockito.when(sparkCluster.getEksCluster()).thenReturn("eks");
        Mockito.when(sparkCluster.getSparkApplicationNamespace()).thenReturn("ns");
        RunningApplicationMonitor monitor = new RunningApplicationMonitor(sparkCluster, timer, new SimpleMeterRegistry());
        SparkApplication curr = new SparkApplication();
        // metadata exists but name is null
        ObjectMeta meta = new ObjectMeta();
        meta.setCreationTimestamp(Instant.now().toString());
        meta.setNamespace("ns");
        curr.setMetadata(meta);
        try (MockedStatic<SparkApplicationResourceHelper> mocked = Mockito.mockStatic(SparkApplicationResourceHelper.class)) {
            mocked.when(() -> SparkApplicationResourceHelper.getState(Mockito.any())).thenReturn("RUNNING");
            monitor.onUpdate(null, curr);
            assertEquals(0, monitor.getApplicationCount());
        }
    }

    @Test
    public void testOnUpdate_runningCreationTimestampNull_skips() {
        timer = new Timer(true);
        AppConfig.SparkCluster sparkCluster = Mockito.mock(AppConfig.SparkCluster.class);
        Mockito.when(sparkCluster.getEksCluster()).thenReturn("eks");
        Mockito.when(sparkCluster.getSparkApplicationNamespace()).thenReturn("ns");
        RunningApplicationMonitor monitor = new RunningApplicationMonitor(sparkCluster, timer, new SimpleMeterRegistry());
        SparkApplication curr = new SparkApplication();
        ObjectMeta meta = new ObjectMeta();
        meta.setName("app-no-ts");
        // creationTimestamp intentionally null
        meta.setNamespace("ns");
        curr.setMetadata(meta);
        try (MockedStatic<SparkApplicationResourceHelper> mocked = Mockito.mockStatic(SparkApplicationResourceHelper.class)) {
            mocked.when(() -> SparkApplicationResourceHelper.getState(Mockito.any())).thenReturn("RUNNING");
            monitor.onUpdate(null, curr);
            assertEquals(0, monitor.getApplicationCount());
        }
    }

    @Test
    public void testOnUpdate_runningCreationTimestampParseFail_skips() {
        timer = new Timer(true);
        AppConfig.SparkCluster sparkCluster = Mockito.mock(AppConfig.SparkCluster.class);
        Mockito.when(sparkCluster.getEksCluster()).thenReturn("eks");
        Mockito.when(sparkCluster.getSparkApplicationNamespace()).thenReturn("ns");
        RunningApplicationMonitor monitor = new RunningApplicationMonitor(sparkCluster, timer, new SimpleMeterRegistry());
        SparkApplication curr = new SparkApplication();
        ObjectMeta meta = new ObjectMeta();
        meta.setName("app-bad-ts");
        meta.setCreationTimestamp("not-a-timestamp");
        meta.setNamespace("ns");
        curr.setMetadata(meta);
        try (MockedStatic<SparkApplicationResourceHelper> mocked = Mockito.mockStatic(SparkApplicationResourceHelper.class)) {
            mocked.when(() -> SparkApplicationResourceHelper.getState(Mockito.any())).thenReturn("RUNNING");
            monitor.onUpdate(null, curr);
            assertEquals(0, monitor.getApplicationCount());
        }
    }

    @Test
    public void testOnUpdate_runningNamespaceNull_skips() {
        timer = new Timer(true);
        AppConfig.SparkCluster sparkCluster = Mockito.mock(AppConfig.SparkCluster.class);
        Mockito.when(sparkCluster.getEksCluster()).thenReturn("eks");
        Mockito.when(sparkCluster.getSparkApplicationNamespace()).thenReturn("ns");
        RunningApplicationMonitor monitor = new RunningApplicationMonitor(sparkCluster, timer, new SimpleMeterRegistry());
        SparkApplication curr = new SparkApplication();
        ObjectMeta meta = new ObjectMeta();
        meta.setName("app-ns-null");
        meta.setCreationTimestamp(Instant.now().toString());
        // namespace intentionally null
        curr.setMetadata(meta);
        try (MockedStatic<SparkApplicationResourceHelper> mocked = Mockito.mockStatic(SparkApplicationResourceHelper.class)) {
            mocked.when(() -> SparkApplicationResourceHelper.getState(Mockito.any())).thenReturn("RUNNING");
            monitor.onUpdate(null, curr);
            assertEquals(0, monitor.getApplicationCount());
        }
    }

    @Test
    public void testOnUpdate_runningSuccessfulAddsApplication() {
        timer = new Timer(true);
        AppConfig.SparkCluster sparkCluster = Mockito.mock(AppConfig.SparkCluster.class);
        Mockito.when(sparkCluster.getEksCluster()).thenReturn("eks");
        Mockito.when(sparkCluster.getSparkApplicationNamespace()).thenReturn("ns");
        RunningApplicationMonitor monitor = new RunningApplicationMonitor(sparkCluster, timer, new SimpleMeterRegistry());
        SparkApplication curr = new SparkApplication();
        ObjectMeta meta = new ObjectMeta();
        meta.setName("app-success");
        meta.setCreationTimestamp(Instant.now().toString());
        meta.setNamespace("ns-success");
        curr.setMetadata(meta);
        try (MockedStatic<SparkApplicationResourceHelper> mocked = Mockito.mockStatic(SparkApplicationResourceHelper.class)) {
            mocked.when(() -> SparkApplicationResourceHelper.getState(Mockito.any())).thenReturn("RUNNING");
            monitor.onUpdate(null, curr);
            assertEquals(1, monitor.getApplicationCount());
            // calling again should keep count at 1 (replace/put same key)
            monitor.onUpdate(null, curr);
            assertEquals(1, monitor.getApplicationCount());
        }
    }
}
