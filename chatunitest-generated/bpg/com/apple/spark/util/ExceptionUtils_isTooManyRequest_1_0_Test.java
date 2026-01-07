package com.apple.spark.util;

import java.lang.reflect.Method;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.apple.spark.core.Constants;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;

class ExceptionUtils_isTooManyRequest_1_0_Test {

    @Test
    void testNullInputReturnsFalse() throws Exception {
        // direct call
        assertFalse(ExceptionUtils.isTooManyRequest(null));
        // reflective call
        Method m = ExceptionUtils.class.getDeclaredMethod("isTooManyRequest", Throwable.class);
        m.setAccessible(true);
        Object result = m.invoke(null, (Object) null);
        assertEquals(Boolean.FALSE, result);
    }

    @Test
    void testMessageContainsPhraseReturnsTrue() throws Exception {
        Throwable t = new RuntimeException("There are too many request errors right now");
        assertTrue(ExceptionUtils.isTooManyRequest(t));
        Method m = ExceptionUtils.class.getDeclaredMethod("isTooManyRequest", Throwable.class);
        m.setAccessible(true);
        Object result = m.invoke(null, t);
        assertEquals(Boolean.TRUE, result);
    }

    @Test
    void testDifferentCaseAndPluralMatches() throws Exception {
        // uppercase and plural form should still match due to toLowerCase().contains(...)
        Throwable t = new Exception("TOO MANY REQUESTS occurred");
        assertTrue(ExceptionUtils.isTooManyRequest(t));
        Method m = ExceptionUtils.class.getDeclaredMethod("isTooManyRequest", Throwable.class);
        m.setAccessible(true);
        Object result = m.invoke(null, t);
        assertEquals(Boolean.TRUE, result);
    }

    @Test
    void testCauseChainMatches() throws Exception {
        Throwable cause = new IllegalStateException("Too Many Request in downstream");
        Throwable t = new Exception("top level message", cause);
        // direct call should find it via cause chain
        assertTrue(ExceptionUtils.isTooManyRequest(t));
        // reflective invocation
        Method m = ExceptionUtils.class.getDeclaredMethod("isTooManyRequest", Throwable.class);
        m.setAccessible(true);
        Object result = m.invoke(null, t);
        assertEquals(Boolean.TRUE, result);
    }

    @Test
    void testDeepCauseChainWithNullMessages() throws Exception {
        // intermediate messages are null, final cause has matching message
        Throwable deep = new Exception("too many request");
        Throwable mid = new Exception((String) null, deep);
        Throwable top = new Exception((String) null, mid);
        assertTrue(ExceptionUtils.isTooManyRequest(top));
        Method m = ExceptionUtils.class.getDeclaredMethod("isTooManyRequest", Throwable.class);
        m.setAccessible(true);
        Object result = m.invoke(null, top);
        assertEquals(Boolean.TRUE, result);
    }

    @Test
    void testNoMatchReturnsFalse() throws Exception {
        Throwable t = new Exception("completely unrelated error message");
        assertFalse(ExceptionUtils.isTooManyRequest(t));
        Method m = ExceptionUtils.class.getDeclaredMethod("isTooManyRequest", Throwable.class);
        m.setAccessible(true);
        Object result = m.invoke(null, t);
        assertEquals(Boolean.FALSE, result);
    }
}
