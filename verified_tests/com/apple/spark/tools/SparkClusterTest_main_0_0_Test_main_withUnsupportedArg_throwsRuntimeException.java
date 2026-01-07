package com.apple.spark.tools;

import com.apple.spark.core.KubernetesHelper;
import com.apple.spark.AppConfig;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

public class SparkClusterTest_main_0_0_Test_main_withUnsupportedArg_throwsRuntimeException {

    private final PrintStream originalOut = System.out;

    @AfterEach
    public void restoreSystemOut() {
        System.setOut(originalOut);
    }


    @Test
    public void main_withUnsupportedArg_throwsRuntimeException() {
        String[] args = new String[] { "-unsupported" };
        RuntimeException ex = assertThrows(RuntimeException.class, () -> SparkClusterTest.main(args));
        assertTrue(ex.getMessage().contains("Unsupported argument"), "Exception should indicate unsupported arg");
    }
}
