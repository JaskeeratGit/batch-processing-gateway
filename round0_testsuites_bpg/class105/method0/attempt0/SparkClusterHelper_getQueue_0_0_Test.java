package com.apple.spark.core;

import com.apple.spark.AppConfig;
import com.apple.spark.api.SubmitApplicationRequest;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.apple.spark.AppConfig.SparkCluster;
import java.util.Optional;
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
 * Unit tests for SparkClusterHelper.getQueue(...) covering branches:
 * - request provides queue (normalize applied)
 * - appConfig provides matched queues (single and multiple)
 * - no matching queue -> default used
 * - secure queue without token -> WebApplicationException thrown
 *
 * Reflection is used to invoke normalizeQueue(...) to satisfy the "use reflection" requirement.
 */
public class SparkClusterHelper_getQueue_0_0_Test {

    private AppConfig appConfig;

    private SubmitApplicationRequest request;

    @BeforeEach
    public void setup() {
        Mockito.reset();
        appConfig = mock(AppConfig.class);
        request = mock(SubmitApplicationRequest.class);
    }

    @Test
    public void testGetQueue_fromRequestUsesNormalizedQueue() throws Exception {
        // request specifies a queue with repeating dots and leading/trailing dots
        when(request.getQueue()).thenReturn("..my..example....queue..");
        // ensure appConfig.getQueues() is null so validateQueueToken short-circuits
        when(appConfig.getQueues()).thenReturn(null);
        // call getQueue normally
        String result = SparkClusterHelper.getQueue(appConfig, request, "alice");
        // expected normalization: repeating dots collapsed and trimmed
        assertEquals("my.example.queue", result);
        // Also demonstrate calling normalizeQueue via reflection
        Class<?> cls = Class.forName("com.apple.spark.core.SparkClusterHelper");
        Method normalize = cls.getDeclaredMethod("normalizeQueue", String.class);
        normalize.setAccessible(true);
        Object normalized = normalize.invoke(null, "..my..example....queue..");
        assertEquals("my.example.queue", normalized);
    }

    @Test
    public void testGetQueue_matchedSingleQueueFromAppConfig() {
        // request does not specify a queue
        when(request.getQueue()).thenReturn(null);
        // prepare a single QueueConfig that contains the user
        AppConfig.QueueConfig qc = mock(AppConfig.QueueConfig.class);
        when(qc.containUser("bob")).thenReturn(true);
        when(qc.getName()).thenReturn("team-bob");
        when(qc.getSecure()).thenReturn(Boolean.FALSE);
        List<AppConfig.QueueConfig> queues = Collections.singletonList(qc);
        when(appConfig.getQueues()).thenReturn(queues);
        // queue token SOPS is irrelevant for non-secure queue; can be null
        when(appConfig.getQueueTokenSOPS()).thenReturn(null);
        String result = SparkClusterHelper.getQueue(appConfig, request, "bob");
        assertEquals("team-bob", result);
    }

    @Test
    public void testGetQueue_multipleMatchedQueuesReturnsOneOfThem() {
        when(request.getQueue()).thenReturn(null);
        AppConfig.QueueConfig qc1 = mock(AppConfig.QueueConfig.class);
        when(qc1.containUser("carol")).thenReturn(true);
        when(qc1.getName()).thenReturn("team-a");
        when(qc1.getSecure()).thenReturn(Boolean.FALSE);
        AppConfig.QueueConfig qc2 = mock(AppConfig.QueueConfig.class);
        when(qc2.containUser("carol")).thenReturn(true);
        when(qc2.getName()).thenReturn("team-b");
        when(qc2.getSecure()).thenReturn(Boolean.FALSE);
        List<AppConfig.QueueConfig> queues = Arrays.asList(qc1, qc2);
        when(appConfig.getQueues()).thenReturn(queues);
        String result = SparkClusterHelper.getQueue(appConfig, request, "carol");
        // result must be one of the two names (shuffle may pick either)
        assertTrue("team-a".equals(result) || "team-b".equals(result));
    }

    @Test
    public void testGetQueue_noMatchingQueuesUsesDefault() {
        when(request.getQueue()).thenReturn(null);
        // queues present but none contain the user
        AppConfig.QueueConfig qc = mock(AppConfig.QueueConfig.class);
        when(qc.containUser("dave")).thenReturn(false);
        when(qc.getName()).thenReturn("some-other-queue");
        when(qc.getSecure()).thenReturn(Boolean.FALSE);
        when(appConfig.getQueues()).thenReturn(Collections.singletonList(qc));
        String result = SparkClusterHelper.getQueue(appConfig, request, "dave");
        assertEquals(SparkClusterHelper.DEFAULT_QUEUE, result);
    }

    @Test
    public void testGetQueue_secureQueueWithoutTokenThrowsBadRequest() {
        when(request.getQueue()).thenReturn(null);
        // request queue token missing
        when(request.getQueueToken()).thenReturn(null);
        // create a queue config that matches the user and is secure
        AppConfig.QueueConfig qc = mock(AppConfig.QueueConfig.class);
        when(qc.containUser("eve")).thenReturn(true);
        when(qc.getName()).thenReturn("secure-queue");
        when(qc.getSecure()).thenReturn(Boolean.TRUE);
        when(appConfig.getQueues()).thenReturn(Collections.singletonList(qc));
        // ensure queueTokenSOPS can be anything (null or non-null). validateQueueToken checks token presence first,
        // so a null token should lead to BAD_REQUEST before checking queueTokenSOPS.
        when(appConfig.getQueueTokenSOPS()).thenReturn(null);
        WebApplicationException ex = assertThrows(WebApplicationException.class, () -> SparkClusterHelper.getQueue(appConfig, request, "eve"));
        // Expect BAD_REQUEST due to missing token (status 400)
        assertEquals(400, ex.getResponse().getStatus());
    }
}
