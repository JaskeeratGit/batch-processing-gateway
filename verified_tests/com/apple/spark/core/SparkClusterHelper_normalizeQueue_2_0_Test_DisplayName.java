package com.apple.spark.core;

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

class SparkClusterHelper_normalizeQueue_2_0_Test_DisplayName {






    @Test
    @DisplayName("normalizeQueue: invocation via reflection returns expected results")
    void testNormalizeQueue_ViaReflection() throws Exception {
        Method m = SparkClusterHelper.class.getDeclaredMethod("normalizeQueue", String.class);
        m.setAccessible(true);
        // static method: invoke with null instance
        Object res1 = m.invoke(null, "a...b...");
        assertEquals("a.b", res1);
        Object res2 = m.invoke(null, ".only.dots.");
        assertEquals("only.dots", res2);
        Object res3 = m.invoke(null, "....");
        assertNull(res3);
    }
}
