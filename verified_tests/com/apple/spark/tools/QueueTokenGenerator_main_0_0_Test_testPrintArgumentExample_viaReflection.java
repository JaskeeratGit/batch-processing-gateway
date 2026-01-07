package com.apple.spark.tools;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.apple.spark.core.QueueTokenVerifier;
import com.apple.spark.util.JwtUtils;

public class QueueTokenGenerator_main_0_0_Test_testPrintArgumentExample_viaReflection {

    private final PrintStream originalOut = System.out;

    private final PrintStream originalErr = System.err;

    private final SecurityManager originalSecurityManager = System.getSecurityManager();

    private ByteArrayOutputStream outContent;

    private ByteArrayOutputStream errContent;

    @BeforeEach
    public void setUpStreams() {
        outContent = new ByteArrayOutputStream();
        errContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    public void restoreStreamsAndSecurityManager() {
        System.setOut(originalOut);
        System.setErr(originalErr);
        System.setSecurityManager(originalSecurityManager);
    }

    // Custom exception to capture System.exit status
    private static class ExitException extends SecurityException {

        final int status;

        ExitException(int status) {
            super("System.exit(" + status + ") called");
            this.status = status;
        }
    }

    // Security manager that prevents System.exit from terminating the JVM
    private static class NoExitSecurityManager extends SecurityManager {

        @Override
        public void checkPermission(java.security.Permission perm) {
            // Allow everything.
        }

        @Override
        public void checkPermission(java.security.Permission perm, Object context) {
            // Allow everything.
        }

        @Override
        public void checkExit(int status) {
            super.checkExit(status);
            throw new ExitException(status);
        }
    }

    @Test
    public void testPrintArgumentExample_viaReflection() throws Exception {
        // Invoke the private printArgumentExample method via reflection and assert output.
        Method m = QueueTokenGenerator.class.getDeclaredMethod("printArgumentExample");
        m.setAccessible(true);
        // Ensure output is empty before invocation
        outContent.reset();
        m.invoke(null);
        String out = outContent.toString().trim();
        assertTrue(out.contains("Argument example for this program: -secret secret1 -subject queueToken1 -queue queue1"), "printArgumentExample should print the usage example");
    }





}
