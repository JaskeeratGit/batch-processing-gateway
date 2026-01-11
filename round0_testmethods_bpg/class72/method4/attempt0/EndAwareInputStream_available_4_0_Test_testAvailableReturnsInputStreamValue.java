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

public class EndAwareInputStream_available_4_0_Test_testAvailableReturnsInputStreamValue {

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
    public void testAvailableReturnsInputStreamValue() throws Throwable {
        // Arrange
        int expectedAvailable = 7;
        FixedAvailableInputStream delegate = new FixedAvailableInputStream(expectedAvailable);
        AtomicBoolean endingActionCalled = new AtomicBoolean(false);
        EndAwareInputStream subject = new EndAwareInputStream(delegate, () -> endingActionCalled.set(true));
        // Use reflection to invoke the available() method (as requested)
        Method availableMethod = EndAwareInputStream.class.getMethod("available");
        // Act
        Object result = availableMethod.invoke(subject);
        // Assert
        assertNotNull(result);
        assertTrue(result instanceof Integer);
        assertEquals(expectedAvailable, ((Integer) result).intValue());
        // In normal success path the ending action should not have been run
        assertFalse(endingActionCalled.get(), "endingAction should not be called on successful available()");
    }

}
