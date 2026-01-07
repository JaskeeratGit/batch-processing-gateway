package com.apple.spark.core;

import com.apple.spark.AppConfig;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.informers.SharedInformerFactory;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static com.apple.spark.core.Constants.*;
import com.apple.spark.operator.DriverSpec;
import com.apple.spark.operator.ExecutorSpec;
import com.apple.spark.operator.SparkApplication;
import com.apple.spark.operator.SparkApplicationSpec;
import com.apple.spark.util.CounterMetricContainer;
import com.apple.spark.util.DateTimeUtils;
import com.apple.spark.util.GaugeMetricContainer;
import com.apple.spark.util.KubernetesClusterAndNamespace;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit tests for ApplicationMonitor.close()
 */
public class ApplicationMonitor_close_2_0_Test_testCloseHandlesExceptionsInInformersAndClients {

    // Helper to set private list fields (informerFactories, clients)
    @SuppressWarnings("unchecked")
    private static <T> List<T> getPrivateListField(Object target, String fieldName) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        return (List<T>) f.get(target);
    }


    @Test
    public void testCloseHandlesExceptionsInInformersAndClients() throws Exception {
        ApplicationMonitor monitor = new ApplicationMonitor(new AppConfig(), new SimpleMeterRegistry());
        // Flags to verify invocation
        AtomicBoolean informerOkCalled = new AtomicBoolean(false);
        AtomicBoolean informerThrowCalled = new AtomicBoolean(false);
        AtomicBoolean clientOkClosed = new AtomicBoolean(false);
        AtomicBoolean clientThrowCalled = new AtomicBoolean(false);
        // Informer that works
        SharedInformerFactory informerOk = (SharedInformerFactory) Proxy.newProxyInstance(SharedInformerFactory.class.getClassLoader(), new Class[] { SharedInformerFactory.class }, new InvocationHandler() {

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if ("stopAllRegisteredInformers".equals(method.getName())) {
                    informerOkCalled.set(true);
                    return null;
                }
                return null;
            }
        });
        // Informer that throws when stopping (to exercise logger.warn branch)
        SharedInformerFactory informerThrow = (SharedInformerFactory) Proxy.newProxyInstance(SharedInformerFactory.class.getClassLoader(), new Class[] { SharedInformerFactory.class }, new InvocationHandler() {

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if ("stopAllRegisteredInformers".equals(method.getName())) {
                    informerThrowCalled.set(true);
                    throw new RuntimeException("stop failure");
                }
                return null;
            }
        });
        // Client that works
        KubernetesClient clientOk = (KubernetesClient) Proxy.newProxyInstance(KubernetesClient.class.getClassLoader(), new Class[] { KubernetesClient.class }, new InvocationHandler() {

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if ("close".equals(method.getName())) {
                    clientOkClosed.set(true);
                    return null;
                }
                return null;
            }
        });
        // Client that throws on close (KubernetesHelper.closeQuietly should swallow)
        KubernetesClient clientThrow = (KubernetesClient) Proxy.newProxyInstance(KubernetesClient.class.getClassLoader(), new Class[] { KubernetesClient.class }, new InvocationHandler() {

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if ("close".equals(method.getName())) {
                    clientThrowCalled.set(true);
                    throw new RuntimeException("close failure");
                }
                return null;
            }
        });
        // Inject into private lists
        List<SharedInformerFactory> informerList = getPrivateListField(monitor, "informerFactories");
        informerList.add(informerOk);
        informerList.add(informerThrow);
        List<KubernetesClient> clients = getPrivateListField(monitor, "clients");
        clients.add(clientOk);
        clients.add(clientThrow);
        // close should not throw despite exceptions in underlying objects
        assertDoesNotThrow(monitor::close);
        // Verify invocations occurred
        assertTrue(informerOkCalled.get(), "informerOk.stopAllRegisteredInformers should be called");
        assertTrue(informerThrowCalled.get(), "informerThrow.stopAllRegisteredInformers should be called (and thrown)");
        assertTrue(clientOkClosed.get(), "clientOk.close should be called");
        assertTrue(clientThrowCalled.get(), "clientThrow.close should be called (and thrown)");
    }
}
