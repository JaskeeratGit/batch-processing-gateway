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

class EndAwareInputStream_read_2_0_Test_testExecuteEndingActionViaReflection_withNonNull_invokesRun {





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
