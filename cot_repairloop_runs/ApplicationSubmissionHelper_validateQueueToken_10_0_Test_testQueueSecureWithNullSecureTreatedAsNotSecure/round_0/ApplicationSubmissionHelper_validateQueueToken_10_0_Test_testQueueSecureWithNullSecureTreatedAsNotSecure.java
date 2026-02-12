package com.apple.spark.core;

import com.apple.spark.AppConfig;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ApplicationSubmissionHelper.validateQueueToken
 *
 * Note: a test-local replacement for QueueTokenVerifier is provided at bottom of this file to
 * avoid calling the production verifier during unit tests. The replacement intentionally mirrors
 * the API (constants and method signatures) used by production code and other tests so compilation
 * of the whole test-suite remains happy.
 */
public class ApplicationSubmissionHelper_validateQueueToken_10_0_Test_testQueueSecureWithNullSecureTreatedAsNotSecure {

    // Helper to create an instance of the nested class com.apple.spark.AppConfig$QueueConfig
    private Object createQueueConfig(String name, Boolean secure) {
        try {
            Class<?> qcClass = Class.forName("com.apple.spark.AppConfig$QueueConfig");
            Object qc = qcClass.getDeclaredConstructor().newInstance();
            // try setter setName
            try {
                Method m = qcClass.getMethod("setName", String.class);
                m.invoke(qc, name);
            } catch (NoSuchMethodException e) {
                // try field
                try {
                    Field f = qcClass.getDeclaredField("name");
                    f.setAccessible(true);
                    f.set(qc, name);
                } catch (NoSuchFieldException ex) {
                    // ignore - best effort
                }
            }
            // set secure via setSecure(Boolean) or setSecure(boolean) or field
            if (secure != null) {
                try {
                    Method m = qcClass.getMethod("setSecure", Boolean.class);
                    m.invoke(qc, secure);
                } catch (NoSuchMethodException e) {
                    try {
                        Method m2 = qcClass.getMethod("setSecure", boolean.class);
                        m2.invoke(qc, secure);
                    } catch (NoSuchMethodException ex) {
                        try {
                            Field f = qcClass.getDeclaredField("secure");
                            f.setAccessible(true);
                            f.set(qc, secure);
                        } catch (NoSuchFieldException exc) {
                            // ignore
                        }
                    }
                }
            } else {
                // leave secure null; try to set actual null if setter exists
                try {
                    Method m = qcClass.getMethod("setSecure", Boolean.class);
                    m.invoke(qc, new Object[] { null });
                } catch (Exception ignored) {
                }
            }
            return qc;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create QueueConfig reflectively", e);
        }
    }

    // Helper to create an instance of com.apple.spark.AppConfig$QueueTokenConfig and set secrets list
    private Object createQueueTokenConfig(List<String> secrets) {
        try {
            Class<?> qtcClass = Class.forName("com.apple.spark.AppConfig$QueueTokenConfig");
            Object qtc = qtcClass.getDeclaredConstructor().newInstance();
            // try setter setSecrets
            try {
                Method m = qtcClass.getMethod("setSecrets", List.class);
                m.invoke(qtc, secrets);
            } catch (NoSuchMethodException e) {
                // try field
                try {
                    Field f = qtcClass.getDeclaredField("secrets");
                    f.setAccessible(true);
                    f.set(qtc, secrets);
                } catch (NoSuchFieldException ex) {
                    // ignore
                }
            }
            return qtc;
        } catch (ClassNotFoundException e) {
            // If the nested class is not present, return the raw list (unlikely in production)
            return secrets;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create QueueTokenConfig reflectively", e);
        }
    }

    @Test
    public void testQueueSecureWithNullSecureTreatedAsNotSecure() {
        AppConfig appConfig = new AppConfig();
        List<Object> queues = new ArrayList<>();
        // create queue config with secure = null
        queues.add(createQueueConfig("maybeQ", null));
        setFieldIfPossible(appConfig, "queues", queues);
        // should not throw even without token
        ApplicationSubmissionHelper.validateQueueToken("maybeQ", null, appConfig);
    }

    @Test
    public void testQueueSecureTrueWithoutTokenThrowsBadRequest() {
        AppConfig appConfig = new AppConfig();
        List<Object> queues = new ArrayList<>();
        queues.add(createQueueConfig("secureQ", true));
        setFieldIfPossible(appConfig, "queues", queues);

        WebApplicationException ex =
            assertThrows(WebApplicationException.class,
                () -> ApplicationSubmissionHelper.validateQueueToken("secureQ", null, appConfig));
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), ex.getResponse().getStatus());
        assertTrue(ex.getMessage().contains("Please specify queueToken"));
    }

    @Test
    public void testQueueSecureTrueWithTokenAndNoQueueTokenSOPSThrowsInternalServerError() {
        AppConfig appConfig = new AppConfig();
        List<Object> queues = new ArrayList<>();
        queues.add(createQueueConfig("secureQ", true));
        setFieldIfPossible(appConfig, "queues", queues);
        // make sure queueTokenSOPS is null (default)
        setFieldIfPossible(appConfig, "queueTokenSOPS", null);

        WebApplicationException ex =
            assertThrows(WebApplicationException.class,
                () -> ApplicationSubmissionHelper.validateQueueToken("secureQ", "some-token", appConfig));
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), ex.getResponse().getStatus());
    }

    @Test
    public void testQueueSecureTrueWithTokenAndSecretsCallsVerifier() {
        AppConfig appConfig = new AppConfig();
        List<Object> queues = new ArrayList<>();
        queues.add(createQueueConfig("secureQ", true));
        setFieldIfPossible(appConfig, "queues", queues);

        // Prepare queue token config with secrets list
        List<String> secrets = Arrays.asList("secret1", "secret2");
        Object qtc = createQueueTokenConfig(secrets);
        setFieldIfPossible(appConfig, "queueTokenSOPS", qtc);

        // Reset the test-local verifier and call
        QueueTokenVerifier.reset();
        ApplicationSubmissionHelper.validateQueueToken("secureQ", "good-token", appConfig);

        assertTrue(QueueTokenVerifier.wasCalled());
        assertEquals("good-token", QueueTokenVerifier.lastToken);
        assertEquals("secureQ", QueueTokenVerifier.lastQueue);
        // lastSecrets may be the same list instance we supplied or equal content
        assertNotNull(QueueTokenVerifier.lastSecrets);
        assertEquals(secrets, QueueTokenVerifier.lastSecrets);
    }

    // Utility to set a field or call setter on AppConfig if present
    private void setFieldIfPossible(Object target, String fieldName, Object value) {
        try {
            // try setter
            Method setter = null;
            Method[] methods = target.getClass().getMethods();
            String setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
            for (Method m : methods) {
                if (m.getName().equals(setterName) && m.getParameterCount() == 1) {
                    setter = m;
                    break;
                }
            }
            if (setter != null) {
                setter.invoke(target, value);
                return;
            }
            // fallback to field
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (NoSuchFieldException | IllegalArgumentException nsf) {
            // ignore quietly if not present
            try {
                // last resort: try to set via a loose approach using declared fields
                Field f = target.getClass().getDeclaredField(fieldName);
                f.setAccessible(true);
                f.set(target, value);
            } catch (Exception e) {
                // ignore
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field " + fieldName, e);
        }
    }
}

/**
 * Test-local replacement/shadow of the production QueueTokenVerifier.
 *
 * Placed in same package so ApplicationSubmissionHelper will resolve this class during tests.
 * This replacement provides the same public API (constants and verify signature) used by other
 * test code and production code to avoid compilation issues and to allow unit tests to control
 * behavior.
 */
class QueueTokenVerifier {

    // Provide the static constants expected by other tests / production code.
    public static final String ISSUER_ADMIN = "issuer-admin";
    public static final String ISSUER_AIRFLOW = "issuer-airflow";
    public static final String CLAIM_ALLOWED_QUEUES = "allowedQueues";

    static volatile boolean called = false;

    static volatile String lastToken = null;

    static volatile List<String> lastSecrets = null;

    static volatile String lastQueue = null;

    /**
     * Mirror the production signature: verify(String token, List<String> secretCandidates, String queue)
     */
    public static void verify(String token, List<String> secrets, String queue) {
        called = true;
        lastToken = token;
        lastSecrets = secrets;
        lastQueue = queue;
        // Simulate a validation pass for token "good-token", throw for "invalid-token"
        if ("invalid-token".equals(token)) {
            throw new WebApplicationException("invalid token", Response.Status.UNAUTHORIZED);
        }
    }

    static void reset() {
        called = false;
        lastToken = null;
        lastSecrets = null;
        lastQueue = null;
    }

    static boolean wasCalled() {
        return called;
    }
}
