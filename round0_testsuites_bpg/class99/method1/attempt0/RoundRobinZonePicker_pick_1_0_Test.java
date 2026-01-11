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

class RoundRobinZonePicker_pick_1_0_Test {

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

    @Test
    void testWrapsFromSizeMinusOneToZero() throws Exception {
        List<String> zones = Arrays.asList("z1", "z2", "z3", "z4");
        setAllowedZones(zones);
        // set index to last element index
        setIndexValue(zones.size() - 1);
        assertEquals(Collections.singletonList("z4"), picker.pick());
        // after pick index should wrap to 0
        assertEquals(0, getIndexValue());
        assertEquals(Collections.singletonList("z1"), picker.pick());
        assertEquals(1, getIndexValue());
    }

    @Test
    void testThrowsNpeWhenAllowedZonesNull() throws Exception {
        // set allowedZones to null
        setAllowedZones(null);
        assertThrows(NullPointerException.class, () -> picker.pick());
    }

    @Test
    void testThrowsArithmeticExceptionWhenAllowedZonesEmpty() throws Exception {
        setAllowedZones(Collections.emptyList());
        // modulo by zero in the update function should throw ArithmeticException
        assertThrows(ArithmeticException.class, () -> picker.pick());
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
