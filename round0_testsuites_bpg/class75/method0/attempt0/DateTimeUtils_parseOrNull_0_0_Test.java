package com.apple.spark.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import java.time.Instant;
import org.slf4j.LoggerFactory;

public class DateTimeUtils_parseOrNull_0_0_Test {

    private Logger originalLogger;

    @AfterEach
    void tearDown() throws Exception {
        // restore original logger if we changed it
        if (originalLogger != null) {
            setLoggerField(originalLogger);
            originalLogger = null;
        }
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

    @Test
    void testParseOrNull_withInvalid_logsDebugAndReturnsNull() throws Exception {
        // replace the private static final logger with a test logger
        TestLogger testLogger = new TestLogger();
        // save original and set new
        originalLogger = getLoggerField();
        setLoggerField(testLogger);
        String bad = "not-a-date";
        Long result = DateTimeUtils.parseOrNull(bad);
        assertNull(result);
        // verify that debug was called with expected message and throwable recorded
        assertTrue(testLogger.debugCalled, "Expected debug to be called on logger");
        assertNotNull(testLogger.lastMessage);
        assertTrue(testLogger.lastMessage.contains(bad), "Expected message to contain the original string");
        assertNotNull(testLogger.lastThrowable, "Expected throwable to be recorded");
    }

    // Reflection helpers
    private Logger getLoggerField() throws Exception {
        Field f = DateTimeUtils.class.getDeclaredField("logger");
        f.setAccessible(true);
        return (Logger) f.get(null);
    }

    private void setLoggerField(Logger newLogger) throws Exception {
        Field f = DateTimeUtils.class.getDeclaredField("logger");
        f.setAccessible(true);
        // remove final modifier
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);
        f.set(null, newLogger);
    }

    // A simple Logger implementation that captures debug calls.
    private static class TestLogger implements Logger {

        volatile boolean debugCalled = false;

        volatile String lastMessage = null;

        volatile Throwable lastThrowable = null;

        @Override
        public String getName() {
            return "TestLogger";
        }

        @Override
        public boolean isTraceEnabled() {
            return false;
        }

        @Override
        public void trace(String msg) {
        }

        @Override
        public void trace(String format, Object arg) {
        }

        @Override
        public void trace(String format, Object arg1, Object arg2) {
        }

        @Override
        public void trace(String format, Object... arguments) {
        }

        @Override
        public void trace(String msg, Throwable t) {
        }

        @Override
        public boolean isTraceEnabled(Marker marker) {
            return false;
        }

        @Override
        public void trace(Marker marker, String msg) {
        }

        @Override
        public void trace(Marker marker, String format, Object arg) {
        }

        @Override
        public void trace(Marker marker, String format, Object arg1, Object arg2) {
        }

        @Override
        public void trace(Marker marker, String format, Object... argArray) {
        }

        @Override
        public void trace(Marker marker, String msg, Throwable t) {
        }

        @Override
        public boolean isDebugEnabled() {
            return true;
        }

        @Override
        public void debug(String msg) {
            this.debugCalled = true;
            this.lastMessage = msg;
        }

        @Override
        public void debug(String format, Object arg) {
            this.debugCalled = true;
            this.lastMessage = format;
        }

        @Override
        public void debug(String format, Object arg1, Object arg2) {
            this.debugCalled = true;
            this.lastMessage = format;
        }

        @Override
        public void debug(String format, Object... arguments) {
            this.debugCalled = true;
            this.lastMessage = format;
        }

        @Override
        public void debug(String msg, Throwable t) {
            this.debugCalled = true;
            this.lastMessage = msg;
            this.lastThrowable = t;
        }

        @Override
        public boolean isDebugEnabled(Marker marker) {
            return true;
        }

        @Override
        public void debug(Marker marker, String msg) {
            debug(msg);
        }

        @Override
        public void debug(Marker marker, String format, Object arg) {
            debug(format, arg);
        }

        @Override
        public void debug(Marker marker, String format, Object arg1, Object arg2) {
            debug(format, arg1, arg2);
        }

        @Override
        public void debug(Marker marker, String format, Object... arguments) {
            debug(format, arguments);
        }

        @Override
        public void debug(Marker marker, String msg, Throwable t) {
            debug(msg, t);
        }

        @Override
        public boolean isInfoEnabled() {
            return false;
        }

        @Override
        public void info(String msg) {
        }

        @Override
        public void info(String format, Object arg) {
        }

        @Override
        public void info(String format, Object arg1, Object arg2) {
        }

        @Override
        public void info(String format, Object... arguments) {
        }

        @Override
        public void info(String msg, Throwable t) {
        }

        @Override
        public boolean isInfoEnabled(Marker marker) {
            return false;
        }

        @Override
        public void info(Marker marker, String msg) {
        }

        @Override
        public void info(Marker marker, String format, Object arg) {
        }

        @Override
        public void info(Marker marker, String format, Object arg1, Object arg2) {
        }

        @Override
        public void info(Marker marker, String format, Object... arguments) {
        }

        @Override
        public void info(Marker marker, String msg, Throwable t) {
        }

        @Override
        public boolean isWarnEnabled() {
            return false;
        }

        @Override
        public void warn(String msg) {
        }

        @Override
        public void warn(String format, Object arg) {
        }

        @Override
        public void warn(String format, Object... arguments) {
        }

        @Override
        public void warn(String format, Object arg1, Object arg2) {
        }

        @Override
        public void warn(String msg, Throwable t) {
        }

        @Override
        public boolean isWarnEnabled(Marker marker) {
            return false;
        }

        @Override
        public void warn(Marker marker, String msg) {
        }

        @Override
        public void warn(Marker marker, String format, Object arg) {
        }

        @Override
        public void warn(Marker marker, String format, Object arg1, Object arg2) {
        }

        @Override
        public void warn(Marker marker, String format, Object... arguments) {
        }

        @Override
        public void warn(Marker marker, String msg, Throwable t) {
        }

        @Override
        public boolean isErrorEnabled() {
            return false;
        }

        @Override
        public void error(String msg) {
        }

        @Override
        public void error(String format, Object arg) {
        }

        @Override
        public void error(String format, Object arg1, Object arg2) {
        }

        @Override
        public void error(String format, Object... arguments) {
        }

        @Override
        public void error(String msg, Throwable t) {
        }

        @Override
        public boolean isErrorEnabled(Marker marker) {
            return false;
        }

        @Override
        public void error(Marker marker, String msg) {
        }

        @Override
        public void error(Marker marker, String format, Object arg) {
        }

        @Override
        public void error(Marker marker, String format, Object arg1, Object arg2) {
        }

        @Override
        public void error(Marker marker, String format, Object... arguments) {
        }

        @Override
        public void error(Marker marker, String msg, Throwable t) {
        }
    }
}
