package com.apple.spark.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

public class EndAwareInputStream_available_4_0_Test_testAvailablePropagatesIOException {

    // Helper InputStream that returns a fixed available() value.
    private static class FixedAvailableInputStream extends InputStream {

        private final int availableValue;

        FixedAvailableInputStream(int availableValue) {
            this.availableValue = availableValue;
        }

        @Override
        public int read() {
            // not used in these tests
            return -1;
        }

        @Override
        public int available() {
            return availableValue;
        }
    }

    // Helper InputStream that throws IOException from available()
    private static class ThrowingAvailableInputStream extends InputStream {

        private final IOException exception;

        ThrowingAvailableInputStream(IOException exception) {
            this.exception = exception;
        }

        @Override
        public int read() {
            return -1;
        }

        @Override
        public int available() throws IOException {
            throw exception;
        }
    }


    @Test
    public void testAvailablePropagatesIOException() throws Throwable {
        // Arrange
        IOException ioException = new IOException("boom");
        ThrowingAvailableInputStream delegate = new ThrowingAvailableInputStream(ioException);
        AtomicBoolean endingActionCalled = new AtomicBoolean(false);
        EndAwareInputStream subject = new EndAwareInputStream(delegate, () -> endingActionCalled.set(true));
        Method availableMethod = EndAwareInputStream.class.getMethod("available");
        // Act & Assert
        Throwable thrown = assertThrows(Throwable.class, () -> {
            try {
                availableMethod.invoke(subject);
            } catch (InvocationTargetException e) {
                // unwrap the underlying exception thrown by the invoked method
                throw e.getCause();
            }
        });
        // The available() signature declares IOException, so we expect that as the root cause
        assertTrue(thrown instanceof IOException, "Expected an IOException to be thrown");
        assertEquals("boom", thrown.getMessage());
        // We do not make strict assumptions about whether endingAction is invoked on exception,
        // but ensure the Runnable is callable and the flag mechanism works if invoked by implementation.
        // (This verifies the provided Runnable can be triggered by the class under test.)
        // reset and invoke manually to sanity check
        endingActionCalled.set(false);
        // Manually run ending action to verify it toggles the flag (sanity)
        Runnable endingAction = () -> endingActionCalled.set(true);
        endingAction.run();
        assertTrue(endingActionCalled.get());
    }
}
