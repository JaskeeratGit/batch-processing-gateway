package com.apple.spark.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

class EndAwareInputStream_read_2_0_Test {

    @Test
    void testReadWhenNotEnd_callsUnderlyingAndDoesNotRunEndingAction() throws Exception {
        AtomicBoolean endingCalled = new AtomicBoolean(false);
        InputStream under = new InputStream() {

            @Override
            public int read() {
                // not used
                return -1;
            }

            @Override
            public int read(byte[] b, int off, int len) {
                // fill with predictable bytes and return 2
                if (len <= 0)
                    return 0;
                int toWrite = Math.min(2, len);
                if (off + toWrite > b.length)
                    return -1;
                b[off] = 11;
                b[off + 1] = 22;
                return toWrite;
            }
        };
        EndAwareInputStream eis = new EndAwareInputStream(under, endingCalled::set);
        byte[] buffer = new byte[4];
        int read = eis.read(buffer, 1, 3);
        assertEquals(2, read, "Should return number of bytes read from underlying stream");
        assertEquals(0, buffer[0], "Byte outside written range unchanged");
        assertEquals(11, buffer[1], "First written byte");
        assertEquals(22, buffer[2], "Second written byte");
        assertFalse(endingCalled.get(), "endingAction should not have been called for non -1 read");
    }

    @Test
    void testReadWhenEnd_invokesEndingAction() throws Exception {
        AtomicBoolean endingCalled = new AtomicBoolean(false);
        InputStream under = new InputStream() {

            @Override
            public int read() {
                return -1;
            }

            @Override
            public int read(byte[] b, int off, int len) {
                // simulate EOF
                return -1;
            }
        };
        EndAwareInputStream eis = new EndAwareInputStream(under, () -> endingCalled.set(true));
        byte[] buffer = new byte[2];
        int read = eis.read(buffer, 0, buffer.length);
        assertEquals(-1, read, "EOF should be propagated");
        assertTrue(endingCalled.get(), "endingAction should be invoked when EOF (-1) is returned");
    }

    @Test
    void testReadWhenUnderlyingThrowsException_endingActionNotInvoked() {
        AtomicBoolean endingCalled = new AtomicBoolean(false);
        InputStream under = new InputStream() {

            @Override
            public int read() throws IOException {
                throw new IOException("single-byte read failure");
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                throw new IOException("bulk read failure");
            }
        };
        EndAwareInputStream eis = new EndAwareInputStream(under, () -> endingCalled.set(true));
        byte[] buffer = new byte[4];
        IOException thrown = assertThrows(IOException.class, () -> eis.read(buffer, 0, buffer.length));
        assertTrue(thrown.getMessage().contains("bulk read failure") || thrown.getMessage().contains("single-byte read failure"));
        assertFalse(endingCalled.get(), "endingAction should not be invoked when an exception is thrown by underlying stream");
    }

    @Test
    void testExecuteEndingActionViaReflection_withNullEndingAction_noException() throws Exception {
        EndAwareInputStream eis = new EndAwareInputStream(new InputStream() {

            @Override
            public int read() {
                return -1;
            }

            @Override
            public int read(byte[] b, int off, int len) {
                return -1;
            }
        }, null);
        Method m = EndAwareInputStream.class.getDeclaredMethod("executeEndingAction");
        m.setAccessible(true);
        // should not throw even though endingAction is null
        m.invoke(eis);
    }

    @Test
    void testExecuteEndingActionViaReflection_withNonNull_invokesRun() throws Exception {
        AtomicInteger counter = new AtomicInteger(0);
        EndAwareInputStream eis = new EndAwareInputStream(new InputStream() {

            @Override
            public int read() {
                return -1;
            }

            @Override
            public int read(byte[] b, int off, int len) {
                return -1;
            }
        }, counter::incrementAndGet);
        Method m = EndAwareInputStream.class.getDeclaredMethod("executeEndingAction");
        m.setAccessible(true);
        m.invoke(eis);
        assertEquals(1, counter.get(), "executeEndingAction should invoke the provided Runnable exactly once");
    }
}
