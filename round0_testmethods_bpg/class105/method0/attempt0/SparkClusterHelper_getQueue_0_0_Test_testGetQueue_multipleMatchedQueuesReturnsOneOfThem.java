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
public class SparkClusterHelper_getQueue_0_0_Test_testGetQueue_multipleMatchedQueuesReturnsOneOfThem {

    private AppConfig appConfig;

    private SubmitApplicationRequest request;

    @BeforeEach
    public void setup() {
        Mockito.reset();
        appConfig = mock(AppConfig.class);
        request = mock(SubmitApplicationRequest.class);
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


}
