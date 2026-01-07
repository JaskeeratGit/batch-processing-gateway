package com.apple.spark.core;

import java.lang.reflect.Field;
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

class RoundRobinZonePicker_pick_1_0_Test_testPickCyclesThroughZones {

    private RoundRobinZonePicker picker;

    private Field allowedZonesField;

    private Field indexField;

    @BeforeEach
    void setUp() throws Exception {
        picker = new RoundRobinZonePicker();
        allowedZonesField = RoundRobinZonePicker.class.getDeclaredField("allowedZones");
        allowedZonesField.setAccessible(true);
        indexField = RoundRobinZonePicker.class.getDeclaredField("index");
        indexField.setAccessible(true);
    }

    private void setAllowedZones(List<String> zones) throws IllegalAccessException {
        allowedZonesField.set(picker, zones);
    }

    private AtomicInteger getIndexAtomic() throws IllegalAccessException {
        return (AtomicInteger) indexField.get(picker);
    }

    private int getIndexValue() throws IllegalAccessException {
        return getIndexAtomic().get();
    }

    private void setIndexValue(int value) throws IllegalAccessException {
        getIndexAtomic().set(value);
    }

    @Test
    void testPickCyclesThroughZones() throws Exception {
        List<String> zones = Arrays.asList("zoneA", "zoneB", "zoneC");
        setAllowedZones(zones);
        // starting index is 0
        assertEquals(Collections.singletonList("zoneA"), picker.pick());
        assertEquals(1, getIndexValue());
        assertEquals(Collections.singletonList("zoneB"), picker.pick());
        assertEquals(2, getIndexValue());
        assertEquals(Collections.singletonList("zoneC"), picker.pick());
        // after 3 picks index should be (0+3)%3 = 0
        assertEquals(0, getIndexValue());
        // cycles back to zoneA
        assertEquals(Collections.singletonList("zoneA"), picker.pick());
        assertEquals(1, getIndexValue());
    }




}
