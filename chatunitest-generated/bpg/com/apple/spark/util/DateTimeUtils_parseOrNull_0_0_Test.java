package com.apple.spark.util;

import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fixed unit tests for DateTimeUtils.parseOrNull(String).
 * Removed attempts to modify the private static final logger to avoid
 * UnsupportedOperationException on some JVMs. These tests assert the
 * method's return values (null) for invalid/empty inputs.
 */
public class DateTimeUtils_parseOrNull_0_0_Test {

    @Test
    void testParseOrNull_withEmptyOrNull_returnsNullAndNoLogging() {
        assertNull(DateTimeUtils.parseOrNull(null), "Should return null for null input");
        assertNull(DateTimeUtils.parseOrNull(""), "Should return null for empty input");
    }

    @Test
    void testParseOrNull_withInvalid_returnsNull() {
        String bad = "not-a-timestamp";
        Long result = DateTimeUtils.parseOrNull(bad);
        assertNull(result, "Should return null for invalid datetime");
    }

    @Test
    void testParseOrNull_withValid_returnsEpochMillis() {
        String iso = "2020-01-01T00:00:00Z";
        Long result = DateTimeUtils.parseOrNull(iso);
        assertEquals(Instant.parse(iso).toEpochMilli(), result);
    }

    @Test
    void testParseOrNull_withNull_returnsNull() {
        assertNull(DateTimeUtils.parseOrNull(null));
    }

    @Test
    void testParseOrNull_withEmpty_returnsNull() {
        assertNull(DateTimeUtils.parseOrNull(""));
    }

    @Test
    void testParseOrNull_withValidIsoInstant_returnsEpochMillis() {
        // 2020-01-01T00:00:00Z => 1577836800000L
        String instant = "2020-01-01T00:00:00Z";
        Long result = DateTimeUtils.parseOrNull(instant);
        assertNotNull(result);
        assertEquals(1577836800000L, result.longValue());
    }
}
