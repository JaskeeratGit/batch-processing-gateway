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

public class QueueTokenGenerator_main_0_0_Test_testValidArgs_printsToken {

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
    public void testValidArgs_printsToken() {
        // For a successful run we do not expect System.exit to be called.
        String[] args = new String[] { "-secret", "s3cr3t", "-subject", "queueToken", "-queue", "queue1" };
        // Ensure no security manager that blocks exit (but there should be no exit for valid args)
        System.setSecurityManager(null);
        QueueTokenGenerator.main(args);
        String out = outContent.toString().trim();
        // The output should be non-empty token value printed on stdout
        assertFalse(out.isEmpty(), "A token should be printed to stdout for valid arguments");
        // Basic sanity: token should not be the usage example or an error message
        assertFalse(out.contains("Argument example for this program"), "Should not print usage");
        assertFalse(errContent.toString().contains("Argument missing"), "Should not print missing errors");
    }
}
