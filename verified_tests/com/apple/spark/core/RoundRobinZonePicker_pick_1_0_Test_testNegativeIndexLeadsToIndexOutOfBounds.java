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

class RoundRobinZonePicker_pick_1_0_Test_testNegativeIndexLeadsToIndexOutOfBounds {

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
    void testNegativeIndexLeadsToIndexOutOfBounds() throws Exception {
        List<String> zones = Arrays.asList("onlyZone");
        setAllowedZones(zones);
        // set index to a negative value, getAndUpdate will return the negative old value and get(...) will fail
        setIndexValue(-1);
        assertThrows(IndexOutOfBoundsException.class, () -> picker.pick());
    }
}
