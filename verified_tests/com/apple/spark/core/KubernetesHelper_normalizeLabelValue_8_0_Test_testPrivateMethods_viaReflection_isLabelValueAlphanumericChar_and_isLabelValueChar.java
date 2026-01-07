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

public class KubernetesHelper_normalizeLabelValue_8_0_Test_testPrivateMethods_viaReflection_isLabelValueAlphanumericChar_and_isLabelValueChar {







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
