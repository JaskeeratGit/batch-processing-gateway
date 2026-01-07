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

class SparkClusterHelper_normalizeQueue_2_0_Test {

    @Test
    @DisplayName("normalizeQueue: no change when already normalized")
    void testNormalizeQueue_NoChange() {
        String input = "a.b.c";
        String actual = SparkClusterHelper.normalizeQueue(input);
        assertEquals("a.b.c", actual);
    }

    @Test
    @DisplayName("normalizeQueue: condense repeating dots")
    void testNormalizeQueue_CondenseRepeatingDots() {
        String input = "a...b";
        String actual = SparkClusterHelper.normalizeQueue(input);
        assertEquals("a.b", actual);
        // multiple repeating groups
        String input2 = "a....b...c";
        String actual2 = SparkClusterHelper.normalizeQueue(input2);
        assertEquals("a.b.c", actual2);
    }

    @Test
    @DisplayName("normalizeQueue: trim leading and trailing dots after condensing")
    void testNormalizeQueue_TrimLeadingTrailingDots() {
        String input = ".a.b.";
        String actual = SparkClusterHelper.normalizeQueue(input);
        assertEquals("a.b", actual);
        String input2 = "...a...b...";
        String actual2 = SparkClusterHelper.normalizeQueue(input2);
        assertEquals("a.b", actual2);
    }

    @Test
    @DisplayName("normalizeQueue: inputs consisting only of dots or empty return null")
    void testNormalizeQueue_OnlyDotsOrEmpty_ReturnsNull() {
        assertNull(SparkClusterHelper.normalizeQueue("."));
        assertNull(SparkClusterHelper.normalizeQueue("...."));
        // empty string has no non-dot char
        assertNull(SparkClusterHelper.normalizeQueue(""));
    }

    @Test
    @DisplayName("normalizeQueue: null input throws NullPointerException")
    void testNormalizeQueue_NullInput_ThrowsNPE() {
        assertThrows(NullPointerException.class, () -> SparkClusterHelper.normalizeQueue(null));
    }

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
