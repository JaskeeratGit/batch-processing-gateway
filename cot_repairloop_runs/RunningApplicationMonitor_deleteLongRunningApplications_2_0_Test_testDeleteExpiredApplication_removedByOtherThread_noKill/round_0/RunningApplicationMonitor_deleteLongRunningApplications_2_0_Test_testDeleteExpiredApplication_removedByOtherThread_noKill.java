package com.apple.spark.core;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.lang.reflect.Constructor;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;

/**
 * Unit tests for RunningApplicationMonitor.deleteLongRunningApplications()
 *
 * Note: This test focuses on exercising deleteLongRunningApplications() without invoking any
 * constructors of RunningApplicationMonitor (to avoid needing real AppConfig types). It uses
 * Mockito (which uses Objenesis) to create an instance that calls real methods and then injects
 * the runningApplications map via reflection. The map contains an "expired" RunningAppInfo
 * instance created reflectively from the actual RunningApplicationMonitor$RunningAppInfo class so
 * that runtime type compatibility is preserved.
 */
public class RunningApplicationMonitor_deleteLongRunningApplications_2_0_Test_testDeleteExpiredApplication_removedByOtherThread_noKill {

    private SimpleMeterRegistry meterRegistry;

    @BeforeEach
    public void setUp() {
        meterRegistry = new SimpleMeterRegistry();
    }

    // A Timer that does not schedule anything (prevents background tasks if a constructor were used)
    private static class NoopTimer extends Timer {

        @Override
        public void schedule(TimerTask task, long delay, long period) {
            // no-op to avoid starting background threads in unit tests
        }
    }

    @Test
    public void testDeleteExpiredApplication_removedByOtherThread_noKill() throws Exception {

        // Create a RunningApplicationMonitor instance that will execute real methods.
        // Mockito (with Objenesis) creates the instance without invoking constructors.
        RunningApplicationMonitor monitor = Mockito.mock(RunningApplicationMonitor.class, Mockito.CALLS_REAL_METHODS);

        // Capture kill attempts instead of performing real Kubernetes operations
        final List<String> killedApps = new ArrayList<>();
        doAnswer(invocation -> {
            String ns = invocation.getArgument(0);
            String name = invocation.getArgument(1);
            killedApps.add(ns + "/" + name);
            return null;
        }).when(monitor).killApplication(anyString(), anyString());

        // Create a map that appears to contain an expired entry but its remove method returns null
        class RemoveReturnsNullMap extends ConcurrentHashMap<Object, Object> {
            @Override
            public Object remove(Object key) {
                // simulate concurrent removal by another thread: return null (no value removed)
                return null;
            }
        }
        RemoveReturnsNullMap map = new RemoveReturnsNullMap();

        // Build an instance of the real nested RunningAppInfo class via reflection so that
        // runtime casting in RunningApplicationMonitor succeeds.
        Class<?> runningAppInfoClass = Class.forName("com.apple.spark.core.RunningApplicationMonitor$RunningAppInfo");

        // Try to find a constructor with two long parameters (creationTimeMillis, maxRunningMillis).
        // Fall back to a no-arg constructor if that is not available.
        Object runningAppInfoInstance;
        try {
            Constructor<?> ctor = runningAppInfoClass.getDeclaredConstructor(long.class, long.class);
            ctor.setAccessible(true);
            // creation time: now - 10s, max running allowed: 1ms -> definitely expired
            runningAppInfoInstance = ctor.newInstance(System.currentTimeMillis() - 10_000L, 1L);
        } catch (NoSuchMethodException e) {
            // try no-arg constructor and then try to set fields reflectively if present
            Constructor<?> ctor = runningAppInfoClass.getDeclaredConstructor();
            ctor.setAccessible(true);
            runningAppInfoInstance = ctor.newInstance();
            // attempt to set creationTimeMillis and maxRunningMillis fields if they exist
            try {
                Field creationField = runningAppInfoClass.getDeclaredField("creationTimeMillis");
                creationField.setAccessible(true);
                creationField.setLong(runningAppInfoInstance, System.currentTimeMillis() - 10_000L);
            } catch (NoSuchFieldException ignored) {
            }
            try {
                Field maxField = runningAppInfoClass.getDeclaredField("maxRunningMillis");
                maxField.setAccessible(true);
                maxField.setLong(runningAppInfoInstance, 1L);
            } catch (NoSuchFieldException ignored) {
            }
        }

        // Create a NamespaceAndName instance from the real class to ensure runtime compatibility
        Class<?> nanClass = Class.forName("com.apple.spark.core.NamespaceAndName");
        Object key;
        try {
            Constructor<?> nanCtor = nanClass.getDeclaredConstructor(String.class, String.class);
            nanCtor.setAccessible(true);
            key = nanCtor.newInstance("concurrent-ns", "concurrent-app");
        } catch (NoSuchMethodException e) {
            // try no-arg + setters if only no-arg exists
            Constructor<?> nanCtor = nanClass.getDeclaredConstructor();
            nanCtor.setAccessible(true);
            key = nanCtor.newInstance();
            try {
                Field nsField = nanClass.getDeclaredField("namespace");
                nsField.setAccessible(true);
                nsField.set(key, "concurrent-ns");
            } catch (NoSuchFieldException ignored) {
            }
            try {
                Field nameField = nanClass.getDeclaredField("name");
                nameField.setAccessible(true);
                nameField.set(key, "concurrent-app");
            } catch (NoSuchFieldException ignored) {
            }
        }

        // Put the reflective RunningAppInfo instance into the map
        map.put(key, runningAppInfoInstance);

        // Inject map into the monitor.runningApplications field
        Field runningAppsField = RunningApplicationMonitor.class.getDeclaredField("runningApplications");
        runningAppsField.setAccessible(true);
        runningAppsField.set(monitor, map);

        // Call the focal method
        monitor.deleteLongRunningApplications();

        // Because remove returned null, killApplication should not be invoked
        assertTrue(killedApps.isEmpty(), "killApplication should not have been called when remove returned null");

        // The map should still contain the entry (since remove returned null)
        assertTrue(map.containsKey(key), "The map should still contain the key when remove returned null");
    }
}
