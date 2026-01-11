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

class EndAwareInputStream_read_2_0_Test_testReadWhenEnd_invokesEndingAction {


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



}
