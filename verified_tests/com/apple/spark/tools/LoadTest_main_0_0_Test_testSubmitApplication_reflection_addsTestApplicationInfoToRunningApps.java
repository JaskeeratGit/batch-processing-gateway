package com.apple.spark.tools;

import com.apple.spark.api.SubmitApplicationResponse;
import com.apple.spark.util.HttpUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.apple.spark.api.GetDriverInfoResponse;
import com.apple.spark.api.GetSubmissionStatusResponse;
import com.apple.spark.api.SubmitApplicationRequest;
import com.apple.spark.core.SparkConstants;
import com.apple.spark.operator.DriverSpec;
import com.apple.spark.operator.ExecutorSpec;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadTest_main_0_0_Test_testSubmitApplication_reflection_addsTestApplicationInfoToRunningApps {

    // Custom SecurityManager to intercept System.exit calls
    static class NoExitSecurityManager extends SecurityManager {

        static class ExitTrappedException extends SecurityException {

            final int status;

            ExitTrappedException(int status) {
                super("System.exit called with status: " + status);
                this.status = status;
            }
        }

        private final SecurityManager previous;

        NoExitSecurityManager(SecurityManager previous) {
            this.previous = previous;
        }

        @Override
        public void checkPermission(Permission perm) {
            // allow everything
        }

        @Override
        public void checkExit(int status) {
            throw new ExitTrappedException(status);
        }

        public SecurityManager getPrevious() {
            return previous;
        }
    }

    @AfterEach
    public void cleanupQueues() throws Exception {
        // Ensure runningApps and finishedApps are cleared between tests
        Field runningField = LoadTest.class.getDeclaredField("runningApps");
        runningField.setAccessible(true);
        ConcurrentLinkedQueue<?> running = (ConcurrentLinkedQueue<?>) runningField.get(null);
        running.clear();
        Field finishedField = LoadTest.class.getDeclaredField("finishedApps");
        finishedField.setAccessible(true);
        ConcurrentLinkedQueue<?> finished = (ConcurrentLinkedQueue<?>) finishedField.get(null);
        finished.clear();
    }



    @Test
    public void testSubmitApplication_reflection_addsTestApplicationInfoToRunningApps() throws Exception {
        // Prepare: clear runningApps
        Field runningField = LoadTest.class.getDeclaredField("runningApps");
        runningField.setAccessible(true);
        @SuppressWarnings("unchecked")
        ConcurrentLinkedQueue<Object> running = (ConcurrentLinkedQueue<Object>) runningField.get(null);
        running.clear();
        // Prepare a fake SubmitApplicationResponse instance and set submissionId
        SubmitApplicationResponse fakeResponse = new SubmitApplicationResponse();
        // Try to set submission id via reflection in case setter exists
        try {
            Method setId = SubmitApplicationResponse.class.getMethod("setSubmissionId", String.class);
            setId.invoke(fakeResponse, "test-submission-123");
        } catch (NoSuchMethodException nsme) {
            // If no setter, try to set field directly
            try {
                Field idField = SubmitApplicationResponse.class.getDeclaredField("submissionId");
                idField.setAccessible(true);
                idField.set(fakeResponse, "test-submission-123");
            } catch (NoSuchFieldException ignored) {
                // If neither exists, still proceed; the mock will be returned and later reflection may fail if TestApplicationInfo expects an id; but most implementations have the setter.
            }
        }
        // Mock static HttpUtils.post to return our fakeResponse
        try (MockedStatic<HttpUtils> mocked = Mockito.mockStatic(HttpUtils.class)) {
            // Use broad matchers for parameters
            mocked.when(() -> HttpUtils.post(anyString(), anyString(), anyString(), anyString(), any(Class.class))).thenReturn(fakeResponse);
            // Invoke private static submitApplication via reflection
            Method submitMethod = LoadTest.class.getDeclaredMethod("submitApplication", int.class, int.class, int.class, int.class);
            submitMethod.setAccessible(true);
            // parameters: applicationIndex, mapTasks, sleepSeconds, executorCount
            submitMethod.invoke(null, 0, 2, 1, 1);
            // Verify that HttpUtils.post was called at least once
            mocked.verify(() -> HttpUtils.post(anyString(), anyString(), anyString(), anyString(), any(Class.class)));
            // Now assert that runningApps contains a TestApplicationInfo with the expected submission id
            // Iterate running queue and inspect each element via reflection
            List<Object> items = new ArrayList<>(running);
            assertFalse(items.isEmpty(), "runningApps should contain at least one TestApplicationInfo");
            boolean found = false;
            for (Object item : items) {
                try {
                    Method getSubmissionId = item.getClass().getMethod("getSubmissionId");
                    Object id = getSubmissionId.invoke(item);
                    if ("test-submission-123".equals(id)) {
                        found = true;
                        break;
                    }
                } catch (NoSuchMethodException nsme) {
                    // fallback: check field named submissionId
                    try {
                        Field f = item.getClass().getDeclaredField("submissionId");
                        f.setAccessible(true);
                        Object id = f.get(item);
                        if ("test-submission-123".equals(id)) {
                            found = true;
                            break;
                        }
                    } catch (NoSuchFieldException ignored) {
                    }
                }
            }
            assertTrue(found, "Expected runningApps to contain TestApplicationInfo with submission id 'test-submission-123'");
        }
    }
}
