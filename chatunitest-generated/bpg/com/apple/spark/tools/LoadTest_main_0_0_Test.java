package com.apple.spark.tools;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.Permission;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.apple.spark.api.GetDriverInfoResponse;
import com.apple.spark.api.GetSubmissionStatusResponse;
import com.apple.spark.api.SubmitApplicationRequest;
import com.apple.spark.api.SubmitApplicationResponse;
import com.apple.spark.core.SparkConstants;
import com.apple.spark.operator.DriverSpec;
import com.apple.spark.operator.ExecutorSpec;
import com.apple.spark.util.HttpUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({ "removal", "unchecked" })
public class LoadTest_main_0_0_Test {

    // Custom SecurityManager to intercept System.exit calls
    static class NoExitSecurityManager extends SecurityManager {

        public static class ExitTrappedException extends SecurityException {

            public final int status;

            public ExitTrappedException(int status) {
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
    public void testMain_withZeroApplications_exitsWithZeroAndNoRunningApps() throws Exception {
        SecurityManager previous = System.getSecurityManager();
        NoExitSecurityManager sm = new NoExitSecurityManager(previous);
        System.setSecurityManager(sm);
        try {
            String[] args = new String[] { "-applications", "0", "-deleteApplication", "false" };
            NoExitSecurityManager.ExitTrappedException ex = assertThrows(NoExitSecurityManager.ExitTrappedException.class, () -> LoadTest.main(args));
            assertEquals(0, ex.status);
            // Verify runningApps and finishedApps are empty
            Field runningField = LoadTest.class.getDeclaredField("runningApps");
            runningField.setAccessible(true);
            ConcurrentLinkedQueue<?> running = (ConcurrentLinkedQueue<?>) runningField.get(null);
            assertNotNull(running);
            assertEquals(0, running.size());
            Field finishedField = LoadTest.class.getDeclaredField("finishedApps");
            finishedField.setAccessible(true);
            ConcurrentLinkedQueue<?> finished = (ConcurrentLinkedQueue<?>) finishedField.get(null);
            assertNotNull(finished);
            assertEquals(0, finished.size());
        } finally {
            System.setSecurityManager(previous);
        }
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

    @Test
    public void testMain_withUnsupportedArgument_throwsRuntimeException() {
        String[] args = new String[] { "-unsupportedArg", "123" };
        // No special SecurityManager needed because exception occurs before System.exit
        RuntimeException ex = assertThrows(RuntimeException.class, () -> LoadTest.main(args));
        assertTrue(ex.getMessage().contains("Unsupported argument"), "Exception message should mention unsupported argument");
    }
}
