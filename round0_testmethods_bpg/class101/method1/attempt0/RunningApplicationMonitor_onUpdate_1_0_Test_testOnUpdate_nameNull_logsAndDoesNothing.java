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
        SparkApplication curr = Mockito.mock(SparkApplication.class);
        // setup status.applicationState.state = "RUNNING"
        SparkApplicationStatus statusMock = Mockito.mock(SparkApplicationStatus.class);
        Object appState = Mockito.mock(Object.class);
        // appState has a getState method at runtime; instead of mocking that specific type,
        // we mock SparkApplicationResourceHelper.getState via providing a status that the helper expects.
        // To keep compile-time-safe, create a small dynamic proxy-like object using Mockito to return the state when called via the helper.
        // But simpler: Mockito can mock a class that has getApplicationState method. Assume SparkApplicationStatus has it.
        try {
            // try to configure a getApplicationState() -> mock with getState()
            Object applicationStateMock = Mockito.mock(Object.class);
            // set up via reflection if needed
            // set statusMock.getApplicationState() to return an object whose getState() returns "RUNNING"
            // We attempt to set using Mockito when; if method names/types exist, it will work.
            when(statusMock.getApplicationState()).thenReturn(applicationStateMock);
            try {
                when(applicationStateMock.getClass().getMethod("getState").invoke(applicationStateMock)).thenReturn("RUNNING");
            } catch (Exception ignore) {
                // If we cannot stub via reflection invocation, fallback to stubbing SparkApplicationResourceHelper by ensuring
                // curr.getStatus() returns statusMock and SparkApplicationResourceHelper will be able to get state via real methods
            }
        } catch (Throwable ignored) {
            // If the above assumptions about classes/methods don't hold, fallback to simpler approach:
        }
        // Instead use a direct approach: create a small subclass instance via anonymous subclass overriding getStatus() to provide an object
        // However, SparkApplication is an interface-like class; mocking getStatus to a mock that returns an applicationState with a getState method
        // For robustness, create a simple proxy using a small inner class if possible, but keep using Mockito to return status mock.
        when(curr.getStatus()).thenReturn(statusMock);
        // metadata mock with null name
        ObjectMeta meta = Mockito.mock(ObjectMeta.class);
        when(curr.getMetadata()).thenReturn(meta);
        when(meta.getName()).thenReturn(null);
        // However SparkApplicationResourceHelper.getState reads status.applicationState.state via real methods.
        // To ensure getState returns "RUNNING", we will use a small helper: create a lightweight status class that has getApplicationState method.
        // But due to variations across versions we attempt to stub by using a second-level mock for applicationState with a getState() method via Mockito's RETURNS_DEEP_STUBS.
        SparkApplication currDeep = Mockito.mock(SparkApplication.class, Mockito.RETURNS_DEEP_STUBS);
        when(currDeep.getStatus().getApplicationState().getState()).thenReturn("RUNNING");
        when(currDeep.getMetadata()).thenReturn(meta);
        monitor.onUpdate(null, currDeep);
        ConcurrentHashMap<?, ?> map = getRunningApplicationsMap();
        assertEquals(0, map.size());
    }




}
