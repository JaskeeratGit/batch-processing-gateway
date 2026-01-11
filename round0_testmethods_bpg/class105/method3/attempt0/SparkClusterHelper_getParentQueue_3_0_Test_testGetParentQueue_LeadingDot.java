package com.apple.spark.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.apple.spark.AppConfig;
import com.apple.spark.AppConfig.SparkCluster;
import com.apple.spark.api.SubmitApplicationRequest;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SparkClusterHelper_getParentQueue_3_0_Test_testGetParentQueue_LeadingDot {

    private Method getGetParentQueueMethod() throws NoSuchMethodException {
        Method m = SparkClusterHelper.class.getDeclaredMethod("getParentQueue", String.class);
        m.setAccessible(true);
        return m;
    }



    @Test
    public void testGetParentQueue_LeadingDot() throws Exception {
        Method m = getGetParentQueueMethod();
        Object result = m.invoke(null, ".queue");
        assertNotNull(result);
        // split(".") yields leading empty segment
        assertEquals("", result);
    }




}
