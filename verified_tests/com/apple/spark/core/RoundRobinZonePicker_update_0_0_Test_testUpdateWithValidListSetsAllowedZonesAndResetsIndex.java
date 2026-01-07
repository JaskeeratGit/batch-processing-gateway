package com.apple.spark.core;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Objects;

class RoundRobinZonePicker_update_0_0_Test_testUpdateWithValidListSetsAllowedZonesAndResetsIndex {

    private RoundRobinZonePicker picker;

    @BeforeEach
    void setUp() {
        picker = new RoundRobinZonePicker();
    }

    @Test
    void testUpdateWithValidListSetsAllowedZonesAndResetsIndex() throws Exception {
        // Prepare a modifiable list and set index to non-zero before update
        List<String> zones = new ArrayList<>(Arrays.asList("zoneA", "zoneB", "zoneC"));
        // Use reflection to set the internal index to a non-zero value
        Field indexField = RoundRobinZonePicker.class.getDeclaredField("index");
        indexField.setAccessible(true);
        AtomicInteger internalIndex = (AtomicInteger) indexField.get(picker);
        // make non-zero
        internalIndex.set(42);
        // Call update with valid list
        picker.update(zones);
        // Verify allowedZones field references the provided list
        Field allowedZonesField = RoundRobinZonePicker.class.getDeclaredField("allowedZones");
        allowedZonesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<String> internalZones = (List<String>) allowedZonesField.get(picker);
        assertSame(zones, internalZones, "allowedZones should reference the provided list");
        // Verify index was reset to 0
        assertEquals(0, internalIndex.get(), "index should be reset to 0 after update");
    }




}
