package com.apple.spark.core;

import org.slf4j.Logger;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationTargetException;
import sun.misc.Unsafe;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.apple.spark.AppConfig;
import com.apple.spark.util.EndAwareInputStream;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.*;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import org.slf4j.LoggerFactory;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * JUnit 5 tests for KubernetesHelper.closeQuietly(Closeable)
 */
public class KubernetesHelper_closeQuietly_7_0_Test {

    private Field loggerField;

    private Logger originalLogger;

    private Logger testLoggerProxy;

    private TestLoggerHandler handler;

    // For Unsafe fallback when JVM forbids setting static final via Field.set(...)
    private Unsafe unsafe;

    private Object staticFieldBase;

    private long staticFieldOffset;

    private boolean usedUnsafe = false;

    @BeforeEach
    public void setUp() throws Throwable {
        // Use reflection to replace the private static final logger field with a proxy logger
        loggerField = KubernetesHelper.class.getDeclaredField("logger");
        loggerField.setAccessible(true);
        // read original logger
        originalLogger = (Logger) loggerField.get(null);
        handler = new TestLoggerHandler();
        testLoggerProxy = (Logger) Proxy.newProxyInstance(Logger.class.getClassLoader(), new Class[] { Logger.class }, handler);
        // Try to remove final modifier (best-effort)
        try {
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(loggerField, loggerField.getModifiers() & ~Modifier.FINAL);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
            // Some JVMs don't allow modifying modifiers; ignore and try other ways
        }
        // Try direct set first; if that fails, fallback to Unsafe
        try {
            loggerField.set(null, testLoggerProxy);
        } catch (Exception | Error e) {
            // Fallback to Unsafe approach
            try {
                Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
                unsafeField.setAccessible(true);
                unsafe = (Unsafe) unsafeField.get(null);
                staticFieldBase = unsafe.staticFieldBase(loggerField);
                staticFieldOffset = unsafe.staticFieldOffset(loggerField);
                unsafe.putObject(staticFieldBase, staticFieldOffset, testLoggerProxy);
                usedUnsafe = true;
            } catch (Throwable t) {
                // If even Unsafe approach fails, rethrow original to make test fail fast
                throw new RuntimeException("Failed to set static final logger field", t);
            }
        }
    }

    @AfterEach
    public void tearDown() throws Throwable {
        // Restore original logger
        if (loggerField != null && originalLogger != null) {
            if (usedUnsafe && unsafe != null) {
                unsafe.putObject(staticFieldBase, staticFieldOffset, originalLogger);
            } else {
                // try normal set
                try {
                    loggerField.set(null, originalLogger);
                } catch (IllegalAccessException e) {
                    // fallback: try Unsafe one more time
                    if (unsafe != null) {
                        unsafe.putObject(staticFieldBase, staticFieldOffset, originalLogger);
                    } else {
                        throw e;
                    }
                }
            }
        }
    }

    @Test
    public void testCloseQuietly_closesSuccessfully_noWarning() {
        Closeable c = new Closeable() {

            boolean closed = false;

            @Override
            public void close() throws IOException {
                closed = true;
            }

            @Override
            public String toString() {
                return "testCloseable";
            }
        };
        KubernetesHelper.closeQuietly(c);
        assertEquals(0, handler.getWarnCount(), "No warnings should be logged when close succeeds");
    }

    @Test
    public void testCloseQuietly_nullArgument_logsAndNoThrow() {
        KubernetesHelper.closeQuietly(null);
        assertEquals(1, handler.getWarnCount(), "One warning should be logged for null argument");
        String msg = handler.getLastMessage();
        assertNotNull(msg);
        assertTrue(msg.contains("Failed to close"), "Message should indicate failure to close");
        assertTrue(msg.contains("null"), "Message should mention 'null'");
        Throwable t = handler.getLastThrowable();
        assertNotNull(t);
        assertTrue(t instanceof NullPointerException, "Throwable should be NullPointerException for null closeable");
    }

    @Test
    public void testCloseQuietly_logsWhenCloseThrowsIOException() {
        Closeable c = new Closeable() {

            @Override
            public void close() throws IOException {
                throw new IOException("io-exception");
            }

            @Override
            public String toString() {
                return "ioCloseable";
            }
        };
        KubernetesHelper.closeQuietly(c);
        assertEquals(1, handler.getWarnCount(), "One warning should be logged when close throws IOException");
        String msg = handler.getLastMessage();
        assertNotNull(msg);
        assertTrue(msg.contains("Failed to close") && msg.contains("ioCloseable"));
        Throwable t = handler.getLastThrowable();
        assertNotNull(t);
        assertTrue(t instanceof IOException);
        assertEquals("io-exception", t.getMessage());
    }

    @Test
    public void testCloseQuietly_logsWhenCloseThrowsError() {
        Closeable c = new Closeable() {

            @Override
            public void close() {
                throw new Error("severe-error");
            }

            @Override
            public String toString() {
                return "errorCloseable";
            }
        };
        KubernetesHelper.closeQuietly(c);
        assertEquals(1, handler.getWarnCount(), "One warning should be logged when close throws Error");
        String msg = handler.getLastMessage();
        assertNotNull(msg);
        assertTrue(msg.contains("Failed to close") && msg.contains("errorCloseable"));
        Throwable t = handler.getLastThrowable();
        assertNotNull(t);
        assertTrue(t instanceof Error);
        assertEquals("severe-error", t.getMessage());
    }

    // InvocationHandler that records warn(...) calls
    private static class TestLoggerHandler implements InvocationHandler {

        private int warnCount = 0;

        private String lastMessage = null;

        private Throwable lastThrowable = null;

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();
            if ("warn".equals(name)) {
                // try to extract message and throwable in common signatures
                if (args != null) {
                    // warn(String msg, Throwable t)
                    if (args.length == 2 && args[0] instanceof String && args[1] instanceof Throwable) {
                        warnCount++;
                        lastMessage = (String) args[0];
                        lastThrowable = (Throwable) args[1];
                        return null;
                    }
                    // warn(String msg)
                    if (args.length == 1 && args[0] instanceof String) {
                        warnCount++;
                        lastMessage = (String) args[0];
                        lastThrowable = null;
                        return null;
                    }
                    // warn(String format, Object... args)
                    if (args.length >= 1 && args[0] instanceof String) {
                        warnCount++;
                        lastMessage = (String) args[0];
                        // try to find throwable in varargs if last arg is throwable
                        Object lastArg = args[args.length - 1];
                        if (lastArg instanceof Throwable) {
                            lastThrowable = (Throwable) lastArg;
                        } else {
                            lastThrowable = null;
                        }
                        return null;
                    }
                }
            }
            // For other methods, return sensible defaults
            Class<?> returnType = method.getReturnType();
            if (returnType == boolean.class) {
                return false;
            }
            if (returnType == int.class) {
                return 0;
            }
            return null;
        }

        int getWarnCount() {
            return warnCount;
        }

        String getLastMessage() {
            return lastMessage;
        }

        Throwable getLastThrowable() {
            return lastThrowable;
        }
    }
}
