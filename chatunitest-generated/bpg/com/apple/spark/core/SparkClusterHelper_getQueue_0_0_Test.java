package com.apple.spark.core;

import com.apple.spark.AppConfig;
import com.apple.spark.api.SubmitApplicationRequest;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.apple.spark.AppConfig.QueueConfig;
import java.util.Optional;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import com.apple.spark.AppConfig.SparkCluster;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fixed unit tests for SparkClusterHelper.getQueue(...) covering branches:
 * - request provides queue (normalize applied)
 * - appConfig provides matched queues (single and multiple)
 * - no matching queue -> default used
 * - secure queue without token -> WebApplicationException thrown (depends on ApplicationSubmissionHelper behavior)
 *
 * Uses MockitoExtension for Mockito lifecycle and reflection to invoke private normalizeQueue(...) method.
 */
@ExtendWith(MockitoExtension.class)
public class SparkClusterHelper_getQueue_0_0_Test {

    @Mock
    private AppConfig appConfig;

    @Mock
    private SubmitApplicationRequest request;

    @Mock
    private AppConfig.QueueConfig queueConfig1;

    @Mock
    private AppConfig.QueueConfig queueConfig2;

    private final String user = "testUser";

    @BeforeEach
    public void setup() {
        // No explicit Mockito.reset() to avoid mock-maker initialization issues in some environments.
        // Mocks are initialized by MockitoExtension.
    }

    @Test
    public void testGetQueue_fromRequestUsesNormalizedQueue() throws Exception {
        // request provides a queue that should be normalized
        when(request.getQueue()).thenReturn(" /root/someQueue ");
        when(request.getQueueToken()).thenReturn(null);
        // appConfig not used in this branch
        when(appConfig.getQueues()).thenReturn(null);
        String queue = SparkClusterHelper.getQueue(appConfig, request, user);
        // Also assert that normalizeQueue (private) produces expected normalized value
        Method m = SparkClusterHelper.class.getDeclaredMethod("normalizeQueue", String.class);
        m.setAccessible(true);
        String normalized = (String) m.invoke(null, " /root/someQueue ");
        assertEquals(normalized, queue);
        // ensure normalization format (assumed behavior)
        assertEquals("root.someQueue", normalized);
    }

    @Test
    public void testGetQueue_matchedSingleQueueFromAppConfig() {
        // request has empty queue
        when(request.getQueue()).thenReturn(null);
        when(request.getQueueToken()).thenReturn(null);
        when(queueConfig1.containUser(user)).thenReturn(true);
        when(queueConfig1.getName()).thenReturn("matchedQueue");
        when(appConfig.getQueues()).thenReturn(Collections.singletonList(queueConfig1));
        String queue = SparkClusterHelper.getQueue(appConfig, request, user);
        assertEquals("matchedQueue", queue);
    }

    @Test
    public void testGetQueue_multipleMatchedQueuesReturnsOneOfThem() {
        when(request.getQueue()).thenReturn(null);
        when(request.getQueueToken()).thenReturn(null);
        when(queueConfig1.containUser(user)).thenReturn(true);
        when(queueConfig1.getName()).thenReturn("matchedQueue1");
        when(queueConfig2.containUser(user)).thenReturn(true);
        when(queueConfig2.getName()).thenReturn("matchedQueue2");
        when(appConfig.getQueues()).thenReturn(Arrays.asList(queueConfig1, queueConfig2));
        String queue = SparkClusterHelper.getQueue(appConfig, request, user);
        assertTrue(queue.equals("matchedQueue1") || queue.equals("matchedQueue2"), "Queue should be one of the matched queues");
    }

    @Test
    public void testGetQueue_noMatchingQueuesUsesDefault() {
        when(request.getQueue()).thenReturn(null);
        when(request.getQueueToken()).thenReturn(null);
        when(queueConfig1.containUser(user)).thenReturn(false);
        when(queueConfig1.getName()).thenReturn("otherQueue");
        when(appConfig.getQueues()).thenReturn(Collections.singletonList(queueConfig1));
        String queue = SparkClusterHelper.getQueue(appConfig, request, user);
        assertEquals(SparkClusterHelper.DEFAULT_QUEUE, queue);
    }

    @Test
    public void testGetQueue_secureQueueWithoutTokenThrowsBadRequest() {
        // This test assumes ApplicationSubmissionHelper.validateQueueToken will throw WebApplicationException
        // when a secure queue is used without a token. We set up a queue name that represents secure.
        when(request.getQueue()).thenReturn("secure.queue");
        when(request.getQueueToken()).thenReturn(null);
        when(appConfig.getQueues()).thenReturn(null);
        try {
            SparkClusterHelper.getQueue(appConfig, request, user);
            fail("Expected WebApplicationException due to missing token for secure queue");
        } catch (WebApplicationException ex) {
            // expected
            assertNotNull(ex.getResponse());
            assertTrue(ex.getResponse().getStatus() == 400 || ex.getResponse().getStatus() == Response.Status.BAD_REQUEST.getStatusCode());
        }
    }
}
