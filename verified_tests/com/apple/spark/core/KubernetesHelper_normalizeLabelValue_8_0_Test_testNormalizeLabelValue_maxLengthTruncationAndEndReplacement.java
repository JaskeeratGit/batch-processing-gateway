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

public class KubernetesHelper_normalizeLabelValue_8_0_Test_testNormalizeLabelValue_maxLengthTruncationAndEndReplacement {






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

}
