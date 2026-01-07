package com.apple.spark.core;

import java.lang.reflect.Constructor;
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

/*
 This test class uses reflection to invoke public and private methods of KubernetesHelper
 to maximize line/branch coverage for getK8sClient(AppConfig.SparkCluster) and the
 private getK8sConfig(AppConfig.SparkCluster) method that it relies on.
*/
public class KubernetesHelper_getK8sClient_4_0_Test_testGetK8sClient_withNullSparkCluster_returnsClient {

    @Test
    public void testGetK8sClient_withNullSparkCluster_returnsClient() throws Exception {
        // Invoke public static getK8sClient(AppConfig.SparkCluster) reflectively with null
        Method getK8sClientMethod = KubernetesHelper.class.getDeclaredMethod("getK8sClient", Class.forName("com.apple.spark.AppConfig$SparkCluster"));
        Object client = getK8sClientMethod.invoke(null, new Object[] { null });
        assertNotNull(client, "getK8sClient(null) should not return null");
        // Assert that returned object's class name matches expected Fabric8 DefaultKubernetesClient
        assertEquals("io.fabric8.kubernetes.client.DefaultKubernetesClient", client.getClass().getName(), "Expected a DefaultKubernetesClient instance");
    }


}
