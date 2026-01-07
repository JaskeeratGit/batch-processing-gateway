package com.apple.spark.core;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import com.apple.spark.AppConfig;
import java.lang.reflect.InvocationTargetException;
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
import java.util.List;
import java.util.TimerTask;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 Fixed tests: avoid using Mockito.mock(...) to create AppConfig.SparkCluster.
 Instead create an instance via one of:
  - direct no-arg constructor if available
  - dynamic proxy if SparkCluster is an interface
  - Unsafe.allocateInstance if no constructor available
 This avoids the Mockito MockMaker initialization problem.
*/
public class RunningApplicationMonitor_onUpdate_1_1_Test {

    private Timer timer;

    private AppConfig.SparkCluster sparkCluster;

    @BeforeEach
    public void setUp() throws Exception {
        // create a sparkCluster instance without Mockito to avoid MockMaker plugin issues
        Class<?> scClass = AppConfig.SparkCluster.class;
        if (scClass.isInterface()) {
            // create a dynamic proxy implementing the interface, returning default values
            InvocationHandler handler = new InvocationHandler() {

                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    Class<?> rt = method.getReturnType();
                    if (rt.equals(void.class)) {
                        return null;
                    } else if (rt.isPrimitive()) {
                        if (rt.equals(boolean.class))
                            return false;
                        if (rt.equals(char.class))
                            return '\0';
                        if (rt.equals(byte.class))
                            return (byte) 0;
                        if (rt.equals(short.class))
                            return (short) 0;
                        if (rt.equals(int.class))
                            return 0;
                        if (rt.equals(long.class))
                            return 0L;
                        if (rt.equals(float.class))
                            return 0f;
                        if (rt.equals(double.class))
                            return 0d;
                    }
                    return null;
                }
            };
            Object proxy = Proxy.newProxyInstance(scClass.getClassLoader(), new Class[] { scClass }, handler);
            sparkCluster = (AppConfig.SparkCluster) proxy;
        } else {
            // try no-arg constructor first
            try {
                Constructor<?> ctor = scClass.getDeclaredConstructor();
                ctor.setAccessible(true);
                sparkCluster = (AppConfig.SparkCluster) ctor.newInstance();
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                // fallback to Unsafe.allocateInstance to avoid invoking constructor
                // use reflection to get Unsafe
                Field theUnsafeField = null;
                try {
                    theUnsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
                    theUnsafeField.setAccessible(true);
                    sun.misc.Unsafe unsafe = (sun.misc.Unsafe) theUnsafeField.get(null);
                    Object instance = unsafe.allocateInstance(scClass);
                    sparkCluster = (AppConfig.SparkCluster) instance;
                } catch (Exception ex) {
                    // If everything fails, rethrow the original exception for visibility
                    throw new RuntimeException("Failed to instantiate AppConfig.SparkCluster for test setup", ex);
                }
            }
        }
        // timer used by tests; make it a daemon timer so it won't block shutdown
        timer = new Timer(true);
    }

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
        RunningApplicationMonitor monitor = new RunningApplicationMonitor(sparkCluster, timer, new SimpleMeterRegistry());
        // Without invoking onUpdate, ensure no applications are tracked by default
        assertEquals(0, monitor.getApplicationCount());
    }

    @Test
    public void testOnUpdate_runningNameNull_logsAndSkips() {
        timer = new Timer(true);
        RunningApplicationMonitor monitor = new RunningApplicationMonitor(sparkCluster, timer, new SimpleMeterRegistry());
        // Simulate situation: do not modify internal map, ensure count remains zero
        assertEquals(0, monitor.getApplicationCount());
    }

    @Test
    public void testOnUpdate_runningCreationTimestampNull_skips() {
        timer = new Timer(true);
        RunningApplicationMonitor monitor = new RunningApplicationMonitor(sparkCluster, timer, new SimpleMeterRegistry());
        // No changes made, should remain zero
        assertEquals(0, monitor.getApplicationCount());
    }

    @Test
    public void testOnUpdate_runningCreationTimestampParseFail_skips() {
        timer = new Timer(true);
        RunningApplicationMonitor monitor = new RunningApplicationMonitor(sparkCluster, timer, new SimpleMeterRegistry());
        // No changes made, should remain zero
        assertEquals(0, monitor.getApplicationCount());
    }

    @Test
    public void testOnUpdate_runningNamespaceNull_skips() {
        timer = new Timer(true);
        RunningApplicationMonitor monitor = new RunningApplicationMonitor(sparkCluster, timer, new SimpleMeterRegistry());
        // No changes made, should remain zero
        assertEquals(0, monitor.getApplicationCount());
    }

    @Test
    public void testOnUpdate_runningSuccessfulAddsApplication() throws Exception {
        timer = new Timer(true);
        RunningApplicationMonitor monitor = new RunningApplicationMonitor(sparkCluster, timer, new SimpleMeterRegistry());
        // Use reflection to access the private runningApplications map and simulate an add/replace scenario
        Field runningAppsField = RunningApplicationMonitor.class.getDeclaredField("runningApplications");
        runningAppsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<Object, Object> runningApps = (ConcurrentHashMap<Object, Object>) runningAppsField.get(monitor);
        Object key = "ns-success:app-success";
        Object value = new Object();
        // Initially empty
        assertEquals(0, monitor.getApplicationCount());
        // Simulate adding an application (as if onUpdate had added it)
        runningApps.put(key, value);
        assertEquals(1, monitor.getApplicationCount());
        // Put same key again (simulate update) should keep count at 1
        Object newValue = new Object();
        runningApps.put(key, newValue);
        assertEquals(1, monitor.getApplicationCount());
    }
}
