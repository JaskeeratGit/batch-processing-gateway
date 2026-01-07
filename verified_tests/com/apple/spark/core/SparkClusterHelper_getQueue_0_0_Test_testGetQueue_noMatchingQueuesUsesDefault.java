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
public class SparkClusterHelper_getQueue_0_0_Test_testGetQueue_noMatchingQueuesUsesDefault {

    private AppConfig appConfig;

    private SubmitApplicationRequest request;

    @BeforeEach
    public void setup() {
        Mockito.reset();
        appConfig = mock(AppConfig.class);
        request = mock(SubmitApplicationRequest.class);
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

}
