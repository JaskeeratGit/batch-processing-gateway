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

public class EndAwareInputStream_markSupported_8_0_Test_testMarkSupportedReturnsFalseAndDelegatesToInputStream {


    @Test
    public void testMarkSupportedReturnsFalseAndDelegatesToInputStream() throws Exception {
        AtomicBoolean runnableCalled = new AtomicBoolean(false);
        Runnable endingAction = () -> runnableCalled.set(true);
        AtomicInteger callCount = new AtomicInteger(0);
        InputStream input = new InputStream() {

            @Override
            public int read() {
                return -1;
            }

            @Override
            public boolean markSupported() {
                callCount.incrementAndGet();
                return false;
            }
        };
        EndAwareInputStream eais = new EndAwareInputStream(input, endingAction);
        // call focal method
        boolean result = eais.markSupported();
        // verify delegation and return value
        assertFalse(result);
        assertEquals(1, callCount.get(), "markSupported should be delegated exactly once");
        // verify endingAction was not invoked by markSupported
        assertFalse(runnableCalled.get(), "endingAction should not be invoked by markSupported");
        // reflectively verify private fields were set to the provided instances
        Field inputField = EndAwareInputStream.class.getDeclaredField("inputStream");
        inputField.setAccessible(true);
        assertSame(input, inputField.get(eais));
        Field endingField = EndAwareInputStream.class.getDeclaredField("endingAction");
        endingField.setAccessible(true);
        assertSame(endingAction, endingField.get(eais));
    }

}
