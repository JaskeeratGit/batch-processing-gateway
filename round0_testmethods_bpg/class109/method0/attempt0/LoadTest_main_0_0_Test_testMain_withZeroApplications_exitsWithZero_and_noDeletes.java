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

public class LoadTest_main_0_0_Test_testMain_withZeroApplications_exitsWithZero_and_noDeletes {

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
    public void testMain_withZeroApplications_exitsWithZero_and_noDeletes() throws Exception {
        // Arrange
        String[] args = new String[] { "-applications", "0", "-deleteApplication", "false", "-mapTasks", "5", "-sleepSeconds", "1", "-maxWaitSeconds", "1", "-executors", "2" };
        // Ensure queues empty
        Field runningField = LoadTest.class.getDeclaredField("runningApps");
        runningField.setAccessible(true);
        ConcurrentLinkedQueue<?> running = (ConcurrentLinkedQueue<?>) runningField.get(null);
        running.clear();
        Field finishedField = LoadTest.class.getDeclaredField("finishedApps");
        finishedField.setAccessible(true);
        ConcurrentLinkedQueue<?> finished = (ConcurrentLinkedQueue<?>) finishedField.get(null);
        finished.clear();
        // Intercept System.exit
        SecurityManager previous = System.getSecurityManager();
        NoExitSecurityManager noExit = new NoExitSecurityManager(previous);
        System.setSecurityManager(noExit);
        try {
            // Act & Assert: Expect System.exit(0) -> ExitTrappedException
            try {
                LoadTest.main(args);
                fail("Expected System.exit to be called");
            } catch (NoExitSecurityManager.ExitTrappedException e) {
                assertEquals(0, e.status, "Expected exit status 0");
            }
            // After exit, ensure no applications were added
            assertEquals(0, running.size(), "runningApps should be empty when applications=0");
            assertEquals(0, finished.size(), "finishedApps should be empty when applications=0");
        } finally {
            System.setSecurityManager(previous);
        }
    }


}
