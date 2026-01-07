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
public class SparkClusterHelper_getQueue_0_0_Test_testGetQueue_secureQueueWithoutTokenThrowsBadRequest {

    private AppConfig appConfig;

    private SubmitApplicationRequest request;

    @BeforeEach
    public void setup() {
        Mockito.reset();
        appConfig = mock(AppConfig.class);
        request = mock(SubmitApplicationRequest.class);
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
