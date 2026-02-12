package com.apple.spark.tools;

import com.apple.spark.AppConfig;
import com.apple.spark.core.KubernetesHelper;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for SparkClusterTest.main(...)
 */
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

        // Create a deep-stubbed client mock so we can stub chained calls without worrying about generic types
        DefaultKubernetesClient clientMock = mock(DefaultKubernetesClient.class, Mockito.RETURNS_DEEP_STUBS);

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

        // Wire deep-stubbed client: client.pods().inNamespace("myns").list() -> podListMock
        when(clientMock.pods().inNamespace("myns").list()).thenReturn(podListMock);

        // Mock static KubernetesHelper.getK8sClient(...) to return our clientMock
        try (MockedStatic<KubernetesHelper> k8sMock = Mockito.mockStatic(KubernetesHelper.class)) {
            k8sMock.when(() -> KubernetesHelper.getK8sClient(Mockito.any(AppConfig.SparkCluster.class)))
                    .thenReturn(clientMock);

            // Act: call main with various args including namespace
            String[] args = new String[] {
                    "-api-server", "https://api",
                    "-user", "u",
                    "-token", "t",
                    "-ca-cert", "ca",
                    "-http-proxy", "http://p",
                    "-https-proxy", "https://p",
                    "-namespace", "myns"
            };
            SparkClusterTest.main(args);

            // Assert: output contains expected lines and pod info
            String output = baos.toString();
            assertTrue(output.contains("Listing pods in cluster https://api namespace myns"),
                    "Should announce listing including master URL and namespace");
            assertTrue(output.contains("Pod pod-a Running"), "Should list pod-a with Running phase");
            assertTrue(output.contains("Pod pod-b Pending"), "Should list pod-b with Pending phase");

            // Also assert KubernetesHelper.getK8sClient was invoked with a SparkCluster instance
            k8sMock.verify(() -> KubernetesHelper.getK8sClient(Mockito.any(AppConfig.SparkCluster.class)));
        }
    }

}
