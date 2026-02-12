package com.apple.spark.core;

import com.apple.spark.AppConfig;
import com.apple.spark.AppConfig.QueueConfig;
import com.apple.spark.AppConfig.QueueTokenConfig;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ApplicationSubmissionHelper.validateQueueToken
 *
 * This test class uses a test-local replacement of QueueTokenVerifier (defined below)
 * in the same package so ApplicationSubmissionHelper resolves that class during tests.
 */
public class ApplicationSubmissionHelperValidateQueueTokenTest {

    @BeforeEach
    public void beforeEach() {
        // reset our test-local verifier state between tests
        QueueTokenVerifier.reset();
    }

    @Test
    public void testQueueNotFoundDoesNothing() {
        AppConfig appConfig = mock(AppConfig.class);
        QueueConfig other = mock(QueueConfig.class);
        when(other.getName()).thenReturn("otherQueue");
        when(other.getSecure()).thenReturn(Boolean.FALSE);
        when(appConfig.getQueues()).thenReturn(Collections.singletonList(other));

        // should not throw even though token is null
        assertDoesNotThrow(() -> ApplicationSubmissionHelper.validateQueueToken("myQueue", null, appConfig));

        // verifier should not be called
        assertFalse(QueueTokenVerifier.wasCalled());
    }

    @Test
    public void testQueueFoundNotSecureDoesNothing() {
        AppConfig appConfig = mock(AppConfig.class);
        QueueConfig q = mock(QueueConfig.class);
        when(q.getName()).thenReturn("myQueue");
        when(q.getSecure()).thenReturn(Boolean.FALSE);
        when(appConfig.getQueues()).thenReturn(Collections.singletonList(q));

        assertDoesNotThrow(() -> ApplicationSubmissionHelper.validateQueueToken("myQueue", null, appConfig));
        assertFalse(QueueTokenVerifier.wasCalled());
    }

    @Test
    public void testQueueSecureTokenNullThrowsBadRequest() {
        AppConfig appConfig = mock(AppConfig.class);
        QueueConfig q = mock(QueueConfig.class);
        when(q.getName()).thenReturn("secureQueue");
        when(q.getSecure()).thenReturn(Boolean.TRUE);
        when(appConfig.getQueues()).thenReturn(Collections.singletonList(q));

        WebApplicationException ex = assertThrows(WebApplicationException.class,
            () -> ApplicationSubmissionHelper.validateQueueToken("secureQueue", null, appConfig));
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), ex.getResponse().getStatus());
    }

    @Test
    public void testQueueSecureNoSopsThrowsInternalServerError() {
        AppConfig appConfig = mock(AppConfig.class);
        QueueConfig q = mock(QueueConfig.class);
        when(q.getName()).thenReturn("secureQueue");
        when(q.getSecure()).thenReturn(Boolean.TRUE);
        when(appConfig.getQueues()).thenReturn(Collections.singletonList(q));
        when(appConfig.getQueueTokenSOPS()).thenReturn(null);

        WebApplicationException ex = assertThrows(WebApplicationException.class,
            () -> ApplicationSubmissionHelper.validateQueueToken("secureQueue", "some-token", appConfig));
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), ex.getResponse().getStatus());
        assertFalse(QueueTokenVerifier.wasCalled());
    }

    @Test
    public void testQueueSecureValidTokenCallsVerifier() {
        AppConfig appConfig = mock(AppConfig.class);
        QueueConfig q = mock(QueueConfig.class);
        QueueTokenConfig qtc = mock(QueueTokenConfig.class);

        when(q.getName()).thenReturn("secureQueue");
        when(q.getSecure()).thenReturn(Boolean.TRUE);
        when(appConfig.getQueues()).thenReturn(Collections.singletonList(q));

        List<String> secrets = Arrays.asList("s1", "s2");
        when(qtc.getSecrets()).thenReturn(secrets);
        when(appConfig.getQueueTokenSOPS()).thenReturn(qtc);

        // call with a token that our test-local verifier treats as valid
        ApplicationSubmissionHelper.validateQueueToken("secureQueue", "good-token", appConfig);

        assertTrue(QueueTokenVerifier.wasCalled());
        assertEquals("good-token", QueueTokenVerifier.lastToken);
        assertEquals(secrets, QueueTokenVerifier.lastSecrets);
        assertEquals("secureQueue", QueueTokenVerifier.lastQueue);
    }

    @Test
    public void testQueueSecureInvalidTokenVerifierThrowsUnauthorized() {
        AppConfig appConfig = mock(AppConfig.class);
        QueueConfig q = mock(QueueConfig.class);
        QueueTokenConfig qtc = mock(QueueTokenConfig.class);

        when(q.getName()).thenReturn("secureQueue");
        when(q.getSecure()).thenReturn(Boolean.TRUE);
        when(appConfig.getQueues()).thenReturn(Collections.singletonList(q));

        List<String> secrets = Arrays.asList("s1");
        when(qtc.getSecrets()).thenReturn(secrets);
        when(appConfig.getQueueTokenSOPS()).thenReturn(qtc);

        WebApplicationException ex = assertThrows(WebApplicationException.class,
            () -> ApplicationSubmissionHelper.validateQueueToken("secureQueue", "invalid-token", appConfig));
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), ex.getResponse().getStatus());

        // verifier was called and recorded the lastToken
        assertTrue(QueueTokenVerifier.wasCalled());
        assertEquals("invalid-token", QueueTokenVerifier.lastToken);
    }
}

/**
 * Test-local replacement/shadow of the production QueueTokenVerifier.
 *
 * Placed in same package so ApplicationSubmissionHelper will resolve this class during tests.
 *
 * This replacement provides the constants and method signatures expected by other tests,
 * and records calls for verification.
 */
class QueueTokenVerifier {

    // Provide the constants that other tests may reference
    public static final String ISSUER_ADMIN = "admin-issuer";
    public static final String ISSUER_AIRFLOW = "airflow-issuer";
    public static final String CLAIM_ALLOWED_QUEUES = "allowed_queues";

    // Recorded state for assertions
    static volatile boolean called = false;
    static volatile String lastToken = null;
    static volatile List<String> lastSecrets = null;
    static volatile String lastQueue = null;

    /**
     * Signature matches production: verify(String token, List<String> secretCandidates, String queue)
     */
    public static void verify(String token, List<String> secretCandidates, String queue) {
        called = true;
        lastToken = token;
        lastSecrets = secretCandidates;
        lastQueue = queue;

        // Simulate validation: "invalid-token" => throw unauthorized, others pass
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
