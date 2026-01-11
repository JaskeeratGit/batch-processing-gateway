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
public class ApplicationMonitor_close_2_0_Test_testCloseStopsAllInformersAndClosesClients_noExceptions {

    // Helper to set private list fields (informerFactories, clients)
    @SuppressWarnings("unchecked")
    private static <T> List<T> getPrivateListField(Object target, String fieldName) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        return (List<T>) f.get(target);
    }

    @Test
    public void testCloseStopsAllInformersAndClosesClients_noExceptions() throws Exception {
        ApplicationMonitor monitor = new ApplicationMonitor(new AppConfig(), new SimpleMeterRegistry());
        // Flags to verify invocation
        AtomicBoolean informer1Called = new AtomicBoolean(false);
        AtomicBoolean informer2Called = new AtomicBoolean(false);
        AtomicBoolean client1Closed = new AtomicBoolean(false);
        AtomicBoolean client2Closed = new AtomicBoolean(false);
        // Create SharedInformerFactory proxies that successfully run stopAllRegisteredInformers
        SharedInformerFactory informer1 = (SharedInformerFactory) Proxy.newProxyInstance(SharedInformerFactory.class.getClassLoader(), new Class[] { SharedInformerFactory.class }, new InvocationHandler() {

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if ("stopAllRegisteredInformers".equals(method.getName())) {
                    informer1Called.set(true);
                    return null;
                }
                // default no-op
                return null;
            }
        });
        SharedInformerFactory informer2 = (SharedInformerFactory) Proxy.newProxyInstance(SharedInformerFactory.class.getClassLoader(), new Class[] { SharedInformerFactory.class }, new InvocationHandler() {

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if ("stopAllRegisteredInformers".equals(method.getName())) {
                    informer2Called.set(true);
                    return null;
                }
                return null;
            }
        });
        // Create KubernetesClient proxies that successfully close
        KubernetesClient client1 = (KubernetesClient) Proxy.newProxyInstance(KubernetesClient.class.getClassLoader(), new Class[] { KubernetesClient.class }, new InvocationHandler() {

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if ("close".equals(method.getName())) {
                    client1Closed.set(true);
                    return null;
                }
                return null;
            }
        });
        KubernetesClient client2 = (KubernetesClient) Proxy.newProxyInstance(KubernetesClient.class.getClassLoader(), new Class[] { KubernetesClient.class }, new InvocationHandler() {

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if ("close".equals(method.getName())) {
                    client2Closed.set(true);
                    return null;
                }
                return null;
            }
        });
        // Inject into private lists
        List<SharedInformerFactory> informerList = getPrivateListField(monitor, "informerFactories");
        informerList.add(informer1);
        informerList.add(informer2);
        List<KubernetesClient> clients = getPrivateListField(monitor, "clients");
        clients.add(client1);
        clients.add(client2);
        // Call close and ensure no exception thrown
        assertDoesNotThrow(monitor::close);
        // Verify that stopAllRegisteredInformers was called for both informers
        assertTrue(informer1Called.get(), "informer1.stopAllRegisteredInformers should be called");
        assertTrue(informer2Called.get(), "informer2.stopAllRegisteredInformers should be called");
        // Verify that clients were closed
        assertTrue(client1Closed.get(), "client1.close should be called");
        assertTrue(client2Closed.get(), "client2.close should be called");
    }

}
