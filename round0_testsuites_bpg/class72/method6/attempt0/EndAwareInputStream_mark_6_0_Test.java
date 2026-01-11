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

public class EndAwareInputStream_mark_6_0_Test {

    @Test
    public void testMarkDelegatesToUnderlyingInputStreamAndFieldsAreSet() throws Exception {
        // prepare a recording InputStream to observe mark invocation
        class RecordingInputStream extends InputStream {

            volatile int lastMark = -1;

            @Override
            public int read() {
                return -1;
            }

            @Override
            public void mark(int readlimit) {
                this.lastMark = readlimit;
            }
        }
        RecordingInputStream underlying = new RecordingInputStream();
        Runnable endingAction = () -> {
            // no-op for this test
        };
        EndAwareInputStream subject = new EndAwareInputStream(underlying, endingAction);
        // Use reflection to invoke the public mark(int) method (also exercises reflective access)
        Method markMethod = EndAwareInputStream.class.getMethod("mark", int.class);
        markMethod.invoke(subject, 123);
        // Verify the underlying stream received the mark call with correct argument
        assertEquals(123, underlying.lastMark, "Underlying InputStream.mark should be called with the provided readlimit");
        // Use reflection to access private fields and verify they were set correctly by the constructor
        Field inputStreamField = EndAwareInputStream.class.getDeclaredField("inputStream");
        inputStreamField.setAccessible(true);
        Object inputStreamValue = inputStreamField.get(subject);
        assertSame(underlying, inputStreamValue, "inputStream field should reference the provided underlying stream");
        Field endingActionField = EndAwareInputStream.class.getDeclaredField("endingAction");
        endingActionField.setAccessible(true);
        Object endingActionValue = endingActionField.get(subject);
        assertSame(endingAction, endingActionValue, "endingAction field should reference the provided Runnable");
    }

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
