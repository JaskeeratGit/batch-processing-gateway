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

class EndAwareInputStream_read_2_0_Test_testExecuteEndingActionViaReflection_withNullEndingAction_noException {




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

}
