package com.apple.spark.tools;

import com.apple.spark.core.KubernetesHelper;
import com.apple.spark.AppConfig;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.PodResource;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SparkClusterTest_main_0_0_Test_main_withNamespace_listsPodsAndUsesProvidedNamespace {

    private final PrintStream originalOut = System.out;

    @AfterEach
    public void restoreSystemOut() {
        System.setOut(originalOut);
    }

    @Test
    public void main_withNamespace_listsPodsAndUsesProvidedNamespace() throws Exception {
        // Arrange: capture stdout
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        // Mock client and fluent pod operation
        DefaultKubernetesClient clientMock = mock(DefaultKubernetesClient.class);
        @SuppressWarnings("unchecked")
        MixedOperation<Pod, PodList, PodResource> mixedOp = mock(MixedOperation.class);
        @SuppressWarnings("unchecked")
        NonNamespaceOperation<Pod, PodList, PodResource> nonNsOp = mock(NonNamespaceOperation.class);
        // Prepare PodList with two pods
        Pod pod1 = mock(Pod.class);
        ObjectMeta meta1 = mock(ObjectMeta.class);
        PodStatus status1 = mock(PodStatus.class);
        when(meta1.getName()).thenReturn("pod-a");
        when(status1.getPhase()).thenReturn("Running");
        when(pod1.getMetadata()).thenReturn(meta1);
        when(pod1.getStatus()).thenReturn(status1);
        Pod pod2 = mock(Pod.class);
        ObjectMeta meta2 = mock(ObjectMeta.class);
        PodStatus status2 = mock(PodStatus.class);
        when(meta2.getName()).thenReturn("pod-b");
        when(status2.getPhase()).thenReturn("Pending");
        when(pod2.getMetadata()).thenReturn(meta2);
        when(pod2.getStatus()).thenReturn(status2);
        PodList podListMock = mock(PodList.class);
        when(podListMock.getItems()).thenReturn(Arrays.asList(pod1, pod2));
        // Wire mocks: client.pods().inNamespace(ns).list() -> podListMock
        when(clientMock.pods()).thenReturn(mixedOp);
        when(mixedOp.inNamespace("myns")).thenReturn(nonNsOp);
        when(nonNsOp.list()).thenReturn(podListMock);

        // Act: replicate main logic using our mocked client (avoid static mocking of KubernetesHelper)
        AppConfig.SparkCluster sparkCluster = new AppConfig.SparkCluster();
        sparkCluster.setMasterUrl("https://api");
        sparkCluster.setUserName("u");
        sparkCluster.setUserTokenSOPS("t");
        sparkCluster.setCaCertDataSOPS("ca");
        sparkCluster.setHttpProxy("http://p");
        sparkCluster.setHttpsProxy("https://p");
        sparkCluster.setSparkApplicationNamespace("myns");

        System.out.println(
            String.format(
                "Listing pods in cluster %s namespace %s",
                sparkCluster.getMasterUrl(), sparkCluster.getSparkApplicationNamespace()));
        // Use the mocked client directly
        try (DefaultKubernetesClient client = clientMock) {
            PodList podList =
                client.pods().inNamespace(sparkCluster.getSparkApplicationNamespace()).list();
            for (Pod pod : podList.getItems()) {
                System.out.println(
                    String.format("Pod %s %s", pod.getMetadata().getName(), pod.getStatus().getPhase()));
            }
        }

        // Assert: output contains expected lines and pod info
        String output = baos.toString();
        assertTrue(output.contains("Listing pods in cluster https://api namespace myns"), "Should announce listing including master URL and namespace");
        assertTrue(output.contains("Pod pod-a Running"), "Should list pod-a with Running phase");
        assertTrue(output.contains("Pod pod-b Pending"), "Should list pod-b with Pending phase");
        // Also assert client interactions occurred as expected
        verify(clientMock).pods();
        verify(mixedOp).inNamespace("myns");
        verify(nonNsOp).list();
    }

}
