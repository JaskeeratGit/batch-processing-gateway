package com.apple.spark.util;

import com.apple.spark.util.EndAwareInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.IOException;

public class EndAwareInputStream_markSupported_8_0_Test_testMarkSupportedPropagatesRuntimeExceptionFromInputStream {



    @Test
    public void testMarkSupportedPropagatesRuntimeExceptionFromInputStream() {
        InputStream input = new InputStream() {

            @Override
            public int read() {
                return -1;
            }

            @Override
            public boolean markSupported() {
                throw new IllegalStateException("forced failure");
            }
        };
        EndAwareInputStream eais = new EndAwareInputStream(input, () -> {
            // no-op
        });
        // verify that exception from underlying inputStream.markSupported() is propagated
        IllegalStateException ex = assertThrows(IllegalStateException.class, eais::markSupported);
        assertEquals("forced failure", ex.getMessage());
    }
}
