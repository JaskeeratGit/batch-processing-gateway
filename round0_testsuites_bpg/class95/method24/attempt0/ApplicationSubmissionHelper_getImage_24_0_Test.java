package com.apple.spark.core;

import static org.mockito.ArgumentMatchers.*;
import com.apple.spark.AppConfig;
import com.apple.spark.api.SubmitApplicationRequest;
import java.lang.reflect.Field;
import java.util.Optional;
import javax.ws.rs.WebApplicationException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static com.apple.spark.core.BatchSchedulerConstants.PLACEHOLDER_TIMEOUT_IN_SECONDS;
import static com.apple.spark.core.BatchSchedulerConstants.YUNIKORN_ROOT_QUEUE;
import static com.apple.spark.core.BatchSchedulerConstants.YUNIKORN_SPARK_DEFAULT_QUEUE;
import static com.apple.spark.core.Constants.*;
import static com.apple.spark.core.SparkConstants.CORE_LIMIT_RATIO;
import static com.apple.spark.core.SparkConstants.DRIVER_CPU_BUFFER_RATIO;
import static com.apple.spark.core.SparkConstants.DRIVER_MEM_BUFFER_RATIO;
import static com.apple.spark.core.SparkConstants.EXECUTOR_CPU_BUFFER_RATIO;
import static com.apple.spark.core.SparkConstants.EXECUTOR_MEM_BUFFER_RATIO;
import static com.apple.spark.core.SparkPodNodeAffinityHelper.createNodeAffinityForSparkPods;
import com.apple.spark.AppConfig.SparkCluster;
import com.apple.spark.operator.Affinity;
import com.apple.spark.operator.BatchSchedulerConfiguration;
import com.apple.spark.operator.DriverSpec;
import com.apple.spark.operator.ExecutorSpec;
import com.apple.spark.operator.NodeAffinity;
import com.apple.spark.operator.SparkApplicationSpec;
import com.apple.spark.operator.SparkUIConfiguration;
import com.apple.spark.operator.Volume;
import com.apple.spark.util.ExceptionUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import io.fabric8.kubernetes.api.model.PodDNSConfig;
import io.fabric8.kubernetes.api.model.PodDNSConfigOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit tests for ApplicationSubmissionHelper.getImage(...)
 */
@ExtendWith(MockitoExtension.class)
public class ApplicationSubmissionHelper_getImage_24_0_Test {

    private static final String TYPE = "scala";

    private static final String VERSION = "2.4";

    private static final String PROXY_USER = "tester";

    private void setRequestImage(SubmitApplicationRequest req, String image) throws Exception {
        Field f = SubmitApplicationRequest.class.getDeclaredField("image");
        f.setAccessible(true);
        f.set(req, image);
    }

    @Test
    public void testGetImage_usesResolvedImage_whenRequestImageIsNull() throws Exception {
        // Arrange
        AppConfig appConfig = mock(AppConfig.class);
        @SuppressWarnings("unchecked")
        AppConfig.SparkImage sparkImage = mock(AppConfig.SparkImage.class);
        when(sparkImage.getName()).thenReturn("resolved-image:2.4");
        when(appConfig.resolveImage(TYPE, VERSION)).thenReturn(Optional.of(sparkImage));
        SubmitApplicationRequest req = new SubmitApplicationRequest();
        // leave image as null (default), ensure internal field null
        setRequestImage(req, null);
        // Act
        String result = ApplicationSubmissionHelper.getImage(appConfig, req, TYPE, VERSION, PROXY_USER);
        // Assert
        assertEquals("resolved-image:2.4", result);
        verify(appConfig, times(1)).resolveImage(TYPE, VERSION);
    }

    @Test
    public void testGetImage_usesResolvedImage_whenRequestImageIsEmpty() throws Exception {
        // Arrange
        AppConfig appConfig = mock(AppConfig.class);
        @SuppressWarnings("unchecked")
        AppConfig.SparkImage sparkImage = mock(AppConfig.SparkImage.class);
        when(sparkImage.getName()).thenReturn("resolved-image-empty:2.4");
        when(appConfig.resolveImage(TYPE, VERSION)).thenReturn(Optional.of(sparkImage));
        SubmitApplicationRequest req = new SubmitApplicationRequest();
        // set empty string
        setRequestImage(req, "");
        // Act
        String result = ApplicationSubmissionHelper.getImage(appConfig, req, TYPE, VERSION, PROXY_USER);
        // Assert
        assertEquals("resolved-image-empty:2.4", result);
        verify(appConfig, times(1)).resolveImage(TYPE, VERSION);
    }

    @Test
    public void testGetImage_returnsCustomImage_whenProvided() throws Exception {
        // Arrange
        AppConfig appConfig = mock(AppConfig.class);
        SubmitApplicationRequest req = new SubmitApplicationRequest();
        String custom = "custom/image:latest";
        setRequestImage(req, custom);
        // Act
        String result = ApplicationSubmissionHelper.getImage(appConfig, req, TYPE, VERSION, PROXY_USER);
        // Assert
        assertEquals(custom, result);
        // ensure resolveImage was not called
        verify(appConfig, never()).resolveImage(anyString(), anyString());
    }

    @Test
    public void testGetImage_throwsBadRequest_whenNoResolvedImageFound() throws Exception {
        // Arrange
        AppConfig appConfig = mock(AppConfig.class);
        when(appConfig.resolveImage(TYPE, VERSION)).thenReturn(Optional.empty());
        SubmitApplicationRequest req = new SubmitApplicationRequest();
        setRequestImage(req, null);
        // Act & Assert
        WebApplicationException ex = assertThrows(WebApplicationException.class, () -> ApplicationSubmissionHelper.getImage(appConfig, req, TYPE, VERSION, PROXY_USER));
        assertTrue(ex.getMessage().contains(String.format("Spark image not found for type: %s, version: %s", TYPE, VERSION)));
    }
}
