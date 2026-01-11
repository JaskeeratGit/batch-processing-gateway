package com.apple.spark.core;

import com.apple.spark.operator.SparkApplication;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static com.apple.spark.core.Constants.MONITOR_KILLED_APPS;
import static com.apple.spark.core.Constants.MONITOR_RUNNING_APPS;
import com.apple.spark.AppConfig;
import com.apple.spark.operator.DriverInfo;
import com.apple.spark.operator.SparkApplicationResourceList;
import com.apple.spark.util.CounterMetricContainer;
import com.apple.spark.util.DateTimeUtils;
import com.apple.spark.util.GaugeMetricContainer;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunningApplicationMonitor_getMaxRunningMillis_0_0_Test {

    // Helper to set metadata on SparkApplication via reflection (works even if metadata is declared in superclass)
    private static void setMetadataReflectively(SparkApplication app, ObjectMeta meta) throws Exception {
        Field field = null;
        Class<?> clazz = app.getClass();
        while (clazz != null) {
            try {
                field = clazz.getDeclaredField("metadata");
                break;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        if (field == null) {
            throw new NoSuchFieldException("Field 'metadata' not found in SparkApplication class hierarchy");
        }
        field.setAccessible(true);
        field.set(app, meta);
    }

    @Test
    public void testGetMaxRunningMillis_MetadataNull() {
        SparkApplication app = new SparkApplication();
        long expected = Constants.DEFAULT_MAX_RUNNING_MILLIS;
        long actual = RunningApplicationMonitor.getMaxRunningMillis(app);
        assertEquals(expected, actual);
    }

    @Test
    public void testGetMaxRunningMillis_LabelsNull() throws Exception {
        SparkApplication app = new SparkApplication();
        ObjectMeta meta = new ObjectMeta();
        // labels left as null
        setMetadataReflectively(app, meta);
        long expected = Constants.DEFAULT_MAX_RUNNING_MILLIS;
        long actual = RunningApplicationMonitor.getMaxRunningMillis(app);
        assertEquals(expected, actual);
    }

    @Test
    public void testGetMaxRunningMillis_LabelMissing() throws Exception {
        SparkApplication app = new SparkApplication();
        ObjectMeta meta = new ObjectMeta();
        // empty labels
        meta.setLabels(new HashMap<>());
        setMetadataReflectively(app, meta);
        long expected = Constants.DEFAULT_MAX_RUNNING_MILLIS;
        long actual = RunningApplicationMonitor.getMaxRunningMillis(app);
        assertEquals(expected, actual);
    }

    @Test
    public void testGetMaxRunningMillis_LabelEmpty() throws Exception {
        SparkApplication app = new SparkApplication();
        ObjectMeta meta = new ObjectMeta();
        Map<String, String> labels = new HashMap<>();
        labels.put(Constants.MAX_RUNNING_MILLIS_LABEL, "");
        meta.setLabels(labels);
        setMetadataReflectively(app, meta);
        long expected = Constants.DEFAULT_MAX_RUNNING_MILLIS;
        long actual = RunningApplicationMonitor.getMaxRunningMillis(app);
        assertEquals(expected, actual);
    }

    @Test
    public void testGetMaxRunningMillis_ParseValid() throws Exception {
        SparkApplication app = new SparkApplication();
        ObjectMeta meta = new ObjectMeta();
        Map<String, String> labels = new HashMap<>();
        labels.put(Constants.MAX_RUNNING_MILLIS_LABEL, "1234567");
        meta.setLabels(labels);
        setMetadataReflectively(app, meta);
        long expected = 1_234_567L;
        long actual = RunningApplicationMonitor.getMaxRunningMillis(app);
        assertEquals(expected, actual);
    }

    @Test
    public void testGetMaxRunningMillis_ParseInvalid() throws Exception {
        SparkApplication app = new SparkApplication();
        ObjectMeta meta = new ObjectMeta();
        meta.setName("my-app");
        Map<String, String> labels = new HashMap<>();
        labels.put(Constants.MAX_RUNNING_MILLIS_LABEL, "not_a_number");
        meta.setLabels(labels);
        setMetadataReflectively(app, meta);
        long expected = Constants.DEFAULT_MAX_RUNNING_MILLIS;
        long actual = RunningApplicationMonitor.getMaxRunningMillis(app);
        assertEquals(expected, actual);
    }
}
