package com.apple.spark.core;

import com.apple.spark.AppConfig;
import com.apple.spark.operator.SparkApplication;
import com.apple.spark.operator.SparkApplicationStatus;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Timer;
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
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit tests for RunningApplicationMonitor.onUpdate(...)
 *
 * These tests use Mockito to create SparkApplication and related objects, and reflection to inspect
 * the private runningApplications map inside RunningApplicationMonitor.
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
        when(sparkClusterMock.getEksCluster()).thenReturn("");
        when(sparkClusterMock.getSparkApplicationNamespace()).thenReturn("");
        // Use a large delete interval to avoid TimerTask triggering during tests
        monitor = new RunningApplicationMonitor(sparkClusterMock, timer, 60 * 60 * 1000L, meterRegistry);
    }

    @AfterEach
    public void teardown() {
        timer.cancel();
    }

    private ConcurrentHashMap<?, ?> getRunningApplicationsMap() throws Exception {
        Field f = RunningApplicationMonitor.class.getDeclaredField("runningApplications");
        f.setAccessible(true);
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<?, ?> map = (ConcurrentHashMap<?, ?>) f.get(monitor);
        return map;
    }


    @Test
    public void testOnUpdate_nameNull_logsAndDoesNothing() throws Exception {
        // Use deep stubs so we can stub nested calls like getStatus().getApplicationState().getState()
        SparkApplication curr = Mockito.mock(SparkApplication.class, Mockito.RETURNS_DEEP_STUBS);
        // metadata mock with null name
        ObjectMeta meta = Mockito.mock(ObjectMeta.class);
        when(curr.getStatus().getApplicationState().getState()).thenReturn("RUNNING");
        when(curr.getMetadata()).thenReturn(meta);
        when(meta.getName()).thenReturn(null);

        monitor.onUpdate(null, curr);
        ConcurrentHashMap<?, ?> map = getRunningApplicationsMap();
        assertEquals(0, map.size());
    }




}
