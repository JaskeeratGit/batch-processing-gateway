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
import java.util.concurrent.ConcurrentHashMap;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fixed unit tests for ZoneManager.pickZones(...)
 *
 * Mockito was removed because of environment mock-maker initialization issues.
 * Tests now use lightweight anonymous subclass stubs and simple implementations.
 */
public class ZoneManager_pickZones_0_0_Test {

    @BeforeEach
    public void clearZonePickers() throws Exception {
        Field zonePickersField = ZoneManager.class.getDeclaredField("zonePickers");
        zonePickersField.setAccessible(true);
        @SuppressWarnings("unchecked")
        ConcurrentMap<String, ZonePicker> map = (ConcurrentMap<String, ZonePicker>) zonePickersField.get(null);
        map.clear();
    }

    @Test
    public void testPickZones_whenRequestIsNull_returnsNull() {
        // both null is fine; method should return null immediately
        List<String> result = ZoneManager.pickZones(null, null, "any", null, "submission");
        assertNull(result);
    }

    @Test
    public void testPickZones_whenAppConfigIsNull_returnsNull() {
        // create a simple non-null SubmitApplicationRequest instance using anonymous subclass
        SubmitApplicationRequest req = new SubmitApplicationRequest() {
        };
        List<String> result = ZoneManager.pickZones(req, null, "any", null, "submission");
        assertNull(result);
    }

    @Test
    public void testPickZones_whenQueueNotFound_returnsNull() {
        // Build an AppConfig stub with one QueueConfig that has a different name
        QueueConfig qc = new QueueConfig() {

            @Override
            public String getName() {
                return "otherQueue";
            }

            @Override
            public List<String> getAllowedZones() {
                return Arrays.asList("zone-x");
            }

            @Override
            public String getZonePickerName() {
                return null;
            }
        };
        AppConfig appConfig = new AppConfig() {

            @Override
            public List<QueueConfig> getQueues() {
                return Collections.singletonList(qc);
            }
        };
        SubmitApplicationRequest req = new SubmitApplicationRequest() {
        };
        List<String> result = ZoneManager.pickZones(req, appConfig, "targetQueue", null, "submission");
        assertNull(result);
    }

    @Test
    public void testPickZones_whenAllowedZonesIsNull_returnsNull() {
        QueueConfig qc = new QueueConfig() {

            @Override
            public String getName() {
                return "qNullZones";
            }

            @Override
            public List<String> getAllowedZones() {
                return null;
            }

            @Override
            public String getZonePickerName() {
                return null;
            }
        };
        AppConfig appConfig = new AppConfig() {

            @Override
            public List<QueueConfig> getQueues() {
                return Collections.singletonList(qc);
            }
        };
        SubmitApplicationRequest req = new SubmitApplicationRequest() {
        };
        List<String> result = ZoneManager.pickZones(req, appConfig, "qNullZones", null, "submission");
        assertNull(result);
    }

    @Test
    public void testPickZones_whenAllowedZonesIsEmpty_returnsNull() {
        QueueConfig qc = new QueueConfig() {

            @Override
            public String getName() {
                return "qEmptyZones";
            }

            @Override
            public List<String> getAllowedZones() {
                return Collections.emptyList();
            }

            @Override
            public String getZonePickerName() {
                return null;
            }
        };
        AppConfig appConfig = new AppConfig() {

            @Override
            public List<QueueConfig> getQueues() {
                return Collections.singletonList(qc);
            }
        };
        SubmitApplicationRequest req = new SubmitApplicationRequest() {
        };
        List<String> result = ZoneManager.pickZones(req, appConfig, "qEmptyZones", null, "submission");
        assertNull(result);
    }

    @Test
    public void testPickZones_success_usesExistingZonePickerFromCache_andReturnsPickedZones() throws Exception {
        // Prepare a simple ZonePicker implementation and put it into the private cache map under key "cachedQueue"
        final List<String> mockedPick = Arrays.asList("zone-a", "zone-b");
        ZonePicker mockPicker = new ZonePicker() {

            @Override
            public List<String> pick() {
                return mockedPick;
            }

            @Override
            public void update(List<String> zones) {
                // no-op for test
            }
        };
        Field zonePickersField = ZoneManager.class.getDeclaredField("zonePickers");
        zonePickersField.setAccessible(true);
        @SuppressWarnings("unchecked")
        ConcurrentMap<String, ZonePicker> map = (ConcurrentMap<String, ZonePicker>) zonePickersField.get(null);
        map.put("cachedQueue", mockPicker);
        // Prepare AppConfig and QueueConfig that matches the key "cachedQueue"
        QueueConfig qc = new QueueConfig() {

            @Override
            public String getName() {
                return "cachedQueue";
            }

            @Override
            public List<String> getAllowedZones() {
                return Arrays.asList("irrelevant-zone");
            }

            @Override
            public String getZonePickerName() {
                return null;
            }
        };
        AppConfig appConfig = new AppConfig() {

            @Override
            public List<QueueConfig> getQueues() {
                return Collections.singletonList(qc);
            }
        };
        SubmitApplicationRequest req = new SubmitApplicationRequest() {
        };
        List<String> result = ZoneManager.pickZones(req, appConfig, "cachedQueue", null, "submission-id-1");
        assertNotNull(result);
        assertEquals(mockedPick, result);
        // Verify the same instance remains in the cache and was used
        assertSame(mockPicker, map.get("cachedQueue"));
    }

    @Test
    public void testCreateZonePickerForQueue_viaReflection_returnsNonNullZonePicker() throws Exception {
        // Prepare a QueueConfig stub with allowed zones and a custom zone picker name (null -> default)
        QueueConfig qc = new QueueConfig() {

            @Override
            public String getName() {
                return "reflectionQueue";
            }

            @Override
            public String getZonePickerName() {
                return null;
            }

            @Override
            public List<String> getAllowedZones() {
                return Arrays.asList("rzone1", "rzone2");
            }
        };
        // Invoke private static createZonePickerForQueue via reflection
        Method method = ZoneManager.class.getDeclaredMethod("createZonePickerForQueue", QueueConfig.class);
        method.setAccessible(true);
        Object returned = method.invoke(null, qc);
        assertNotNull(returned);
        assertTrue(returned instanceof ZonePicker);
    }
}
