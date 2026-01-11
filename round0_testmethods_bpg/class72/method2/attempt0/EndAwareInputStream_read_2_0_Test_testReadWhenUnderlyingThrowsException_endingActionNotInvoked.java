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

class EndAwareInputStream_read_2_0_Test_testReadWhenUnderlyingThrowsException_endingActionNotInvoked {



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


}
