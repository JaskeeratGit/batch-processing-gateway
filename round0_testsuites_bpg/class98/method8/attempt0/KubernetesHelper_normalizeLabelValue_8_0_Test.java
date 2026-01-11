package com.apple.spark.core;

import java.lang.reflect.Method;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.apple.spark.AppConfig;
import com.apple.spark.util.EndAwareInputStream;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.*;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.Closeable;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class KubernetesHelper_normalizeLabelValue_8_0_Test {

    @Test
    public void testNormalizeLabelValue_null() {
        assertNull(KubernetesHelper.normalizeLabelValue(null));
    }

    @Test
    public void testNormalizeLabelValue_empty() {
        assertEquals("", KubernetesHelper.normalizeLabelValue(""));
    }

    @Test
    public void testNormalizeLabelValue_leadingNonAlphanumeric() {
        String input = "?abc";
        String expected = "0abc";
        assertEquals(expected, KubernetesHelper.normalizeLabelValue(input));
    }

    @Test
    public void testNormalizeLabelValue_middleNonAllowedChar() {
        String input = "a$b";
        String expected = "a0b";
        assertEquals(expected, KubernetesHelper.normalizeLabelValue(input));
    }

    @Test
    public void testNormalizeLabelValue_lastCharReplacedIfNotAlphanumeric() {
        String input = "ab-";
        String expected = "ab0";
        assertEquals(expected, KubernetesHelper.normalizeLabelValue(input));
    }

    @Test
    public void testNormalizeLabelValue_maxLengthTruncationAndEndReplacement() {
        // Construct input where the resulting sb reaches MAX_LABEL_VALUE_LENGTH
        // and the last character collected is a non-alphanumeric ('-'),
        // which should be replaced with '0' after truncation.
        int max = KubernetesHelper.MAX_LABEL_VALUE_LENGTH;
        // 'a' + 62 '-' -> total 63 chars before adding extra char
        String input = "a" + "-".repeat(max - 1) + "extra";
        // After building, sb contains 'a' + (max-1) '-' (length == max), so the last char is '-'
        // It should be replaced by '0'.
        String expected = "a" + "-".repeat(max - 2) + "0";
        String actual = KubernetesHelper.normalizeLabelValue(input);
        assertEquals(max, actual.length(), "Result should be truncated to MAX_LABEL_VALUE_LENGTH");
        assertEquals(expected, actual);
    }

    @Test
    public void testPrivateMethods_viaReflection_isLabelValueAlphanumericChar_and_isLabelValueChar() throws Exception {
        Method isAlnumMethod = KubernetesHelper.class.getDeclaredMethod("isLabelValueAlphanumericChar", char.class);
        Method isValueCharMethod = KubernetesHelper.class.getDeclaredMethod("isLabelValueChar", char.class);
        isAlnumMethod.setAccessible(true);
        isValueCharMethod.setAccessible(true);
        // isLabelValueAlphanumericChar: digits and letters are true; symbols false
        assertTrue((Boolean) isAlnumMethod.invoke(null, 'a'));
        assertTrue((Boolean) isAlnumMethod.invoke(null, 'Z'));
        assertTrue((Boolean) isAlnumMethod.invoke(null, '0'));
        assertFalse((Boolean) isAlnumMethod.invoke(null, '-'));
        assertFalse((Boolean) isAlnumMethod.invoke(null, '.'));
        // isLabelValueChar: alphanumeric, '-' '_' '.' are allowed
        assertTrue((Boolean) isValueCharMethod.invoke(null, 'a'));
        assertTrue((Boolean) isValueCharMethod.invoke(null, '9'));
        assertTrue((Boolean) isValueCharMethod.invoke(null, '-'));
        assertTrue((Boolean) isValueCharMethod.invoke(null, '_'));
        assertTrue((Boolean) isValueCharMethod.invoke(null, '.'));
        assertFalse((Boolean) isValueCharMethod.invoke(null, '@'));
        assertFalse((Boolean) isValueCharMethod.invoke(null, ' '));
    }
}
