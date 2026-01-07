package com.apple.spark.util;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.IOException;

public class EndAwareInputStream_mark_6_0_Test_testMarkPropagatesRuntimeExceptionFromUnderlyingStream {


    @Test
    public void testMarkPropagatesRuntimeExceptionFromUnderlyingStream() {
        InputStream bad = new InputStream() {

            @Override
            public int read() {
                return -1;
            }

            @Override
            public void mark(int readlimit) {
                throw new IllegalStateException("boom");
            }
        };
        EndAwareInputStream subject = new EndAwareInputStream(bad, () -> {
        });
        // Direct call so we receive the real runtime exception (no reflection wrapping)
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> subject.mark(5));
        assertEquals("boom", ex.getMessage());
    }
}
