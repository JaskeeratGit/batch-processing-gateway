package com.apple.spark.core;

import com.apple.spark.AppConfig;
import com.apple.spark.AppConfig.QueueConfig;
import com.apple.spark.api.SubmitApplicationRequest;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZoneManager_pickZones_0_0_Test_testCreateZonePickerForQueue_viaReflection_returnsNonNullZonePicker {

    @BeforeEach
    public void clearZonePickers() throws Exception {
        Field zonePickersField = ZoneManager.class.getDeclaredField("zonePickers");
        zonePickersField.setAccessible(true);
        @SuppressWarnings("unchecked")
        ConcurrentMap<String, ZonePicker> map = (ConcurrentMap<String, ZonePicker>) zonePickersField.get(null);
        map.clear();
    }







    @Test
    public void testCreateZonePickerForQueue_viaReflection_returnsNonNullZonePicker() throws Exception {
        // Prepare a QueueConfig mock with allowed zones and a custom zone picker name (null -> default)
        QueueConfig qc = Mockito.mock(QueueConfig.class);
        when(qc.getName()).thenReturn("reflectionQueue");
        // should use default round_robin
        when(qc.getZonePickerName()).thenReturn(null);
        when(qc.getAllowedZones()).thenReturn(Arrays.asList("rzone1", "rzone2"));
        // Invoke private static createZonePickerForQueue via reflection
        Method method = ZoneManager.class.getDeclaredMethod("createZonePickerForQueue", QueueConfig.class);
        method.setAccessible(true);
        Object returned = method.invoke(null, qc);
        assertNotNull(returned);
        assertTrue(returned instanceof ZonePicker);
    }
}
