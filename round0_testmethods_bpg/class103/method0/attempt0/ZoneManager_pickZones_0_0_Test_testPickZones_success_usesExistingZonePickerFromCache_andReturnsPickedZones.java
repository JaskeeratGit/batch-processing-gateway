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

public class ZoneManager_pickZones_0_0_Test_testPickZones_success_usesExistingZonePickerFromCache_andReturnsPickedZones {

    @BeforeEach
    public void clearZonePickers() throws Exception {
        Field zonePickersField = ZoneManager.class.getDeclaredField("zonePickers");
        zonePickersField.setAccessible(true);
        @SuppressWarnings("unchecked")
        ConcurrentMap<String, ZonePicker> map = (ConcurrentMap<String, ZonePicker>) zonePickersField.get(null);
        map.clear();
    }






    @Test
    public void testPickZones_success_usesExistingZonePickerFromCache_andReturnsPickedZones() throws Exception {
        // Prepare a mock ZonePicker and put it into the private cache map under key "cachedQueue"
        ZonePicker mockPicker = Mockito.mock(ZonePicker.class);
        List<String> mockedPick = Arrays.asList("zone-a", "zone-b");
        when(mockPicker.pick()).thenReturn(mockedPick);
        // Put mock into ZoneManager.zonePickers via reflection
        Field zonePickersField = ZoneManager.class.getDeclaredField("zonePickers");
        zonePickersField.setAccessible(true);
        @SuppressWarnings("unchecked")
        ConcurrentMap<String, ZonePicker> map = (ConcurrentMap<String, ZonePicker>) zonePickersField.get(null);
        map.put("cachedQueue", mockPicker);
        // Prepare AppConfig and QueueConfig that matches the key "cachedQueue"
        SubmitApplicationRequest req = Mockito.mock(SubmitApplicationRequest.class);
        AppConfig appConfig = Mockito.mock(AppConfig.class);
        QueueConfig qc = Mockito.mock(QueueConfig.class);
        when(qc.getName()).thenReturn("cachedQueue");
        // allowed zones must be non-null and non-empty to proceed to computeIfAbsent logic
        when(qc.getAllowedZones()).thenReturn(Arrays.asList("irrelevant-zone"));
        when(appConfig.getQueues()).thenReturn(Collections.singletonList(qc));
        List<String> result = ZoneManager.pickZones(req, appConfig, "cachedQueue", null, "submission-id-1");
        assertNotNull(result);
        assertEquals(mockedPick, result);
        // Verify the same instance remains in the cache and was used
        assertSame(mockPicker, map.get("cachedQueue"));
    }

}
