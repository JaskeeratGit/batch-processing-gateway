package com.apple.spark.core;

import com.apple.spark.util.JwtUtils;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueueTokenVerifier_verify_0_0_Test {

    private final String TOKEN = "dummy-token";

    private final String QUEUE = "targetQueue";

    @Test
    public void testVerify_nullSecretCandidates_throwsInternalServerError() {
        WebApplicationException ex = assertThrows(WebApplicationException.class, () -> QueueTokenVerifier.verify(TOKEN, null, QUEUE));
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), ex.getResponse().getStatus());
        assertTrue(ex.getMessage().contains("Server not configured with secrets to verify queue token"));
    }

    @Test
    public void testVerify_emptySecretCandidates_throwsInternalServerError() {
        WebApplicationException ex = assertThrows(WebApplicationException.class, () -> QueueTokenVerifier.verify(TOKEN, Collections.emptyList(), QUEUE));
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), ex.getResponse().getStatus());
        assertTrue(ex.getMessage().contains("Server not configured with secrets to verify queue token"));
    }

    @Test
    public void testVerify_allSecretsFail_withNonJWTException_throwsInternalServerError() {
        List<String> secrets = Arrays.asList("s1", "s2");
        try (MockedStatic<JwtUtils> jwtMock = Mockito.mockStatic(JwtUtils.class)) {
            jwtMock.when(() -> JwtUtils.verifyToken(TOKEN, "s1")).thenThrow(new RuntimeException("boom1"));
            jwtMock.when(() -> JwtUtils.verifyToken(TOKEN, "s2")).thenThrow(new IllegalStateException("boom2"));
            WebApplicationException ex = assertThrows(WebApplicationException.class, () -> QueueTokenVerifier.verify(TOKEN, secrets, QUEUE));
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), ex.getResponse().getStatus());
            assertTrue(ex.getMessage().contains("Server failed to verify queue token"));
        }
    }

    @Test
    public void testVerify_allSecretsFail_withJWTVerificationException_throwsBadRequest() {
        List<String> secrets = Arrays.asList("s1", "s2");
        try (MockedStatic<JwtUtils> jwtMock = Mockito.mockStatic(JwtUtils.class)) {
            jwtMock.when(() -> JwtUtils.verifyToken(TOKEN, "s1")).thenThrow(new JWTVerificationException("invalid token"));
            jwtMock.when(() -> JwtUtils.verifyToken(TOKEN, "s2")).thenThrow(new JWTVerificationException("still invalid"));
            WebApplicationException ex = assertThrows(WebApplicationException.class, () -> QueueTokenVerifier.verify(TOKEN, secrets, QUEUE));
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), ex.getResponse().getStatus());
            assertTrue(ex.getMessage().contains("Queue token is not valid"));
        }
    }

    @Test
    public void testVerify_jwtWithNullIssuer_throwsBadRequest() {
        List<String> secrets = Arrays.asList("s");
        DecodedJWT jwt = mock(DecodedJWT.class);
        when(jwt.getIssuer()).thenReturn(null);
        try (MockedStatic<JwtUtils> jwtMock = Mockito.mockStatic(JwtUtils.class)) {
            jwtMock.when(() -> JwtUtils.verifyToken(TOKEN, "s")).thenReturn(jwt);
            WebApplicationException ex = assertThrows(WebApplicationException.class, () -> QueueTokenVerifier.verify(TOKEN, secrets, QUEUE));
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), ex.getResponse().getStatus());
            assertTrue(ex.getMessage().contains("no issuer"));
        }
    }

    @Test
    public void testVerify_jwtWithEmptyIssuer_throwsBadRequest() {
        List<String> secrets = Arrays.asList("s");
        DecodedJWT jwt = mock(DecodedJWT.class);
        when(jwt.getIssuer()).thenReturn("");
        try (MockedStatic<JwtUtils> jwtMock = Mockito.mockStatic(JwtUtils.class)) {
            jwtMock.when(() -> JwtUtils.verifyToken(TOKEN, "s")).thenReturn(jwt);
            WebApplicationException ex = assertThrows(WebApplicationException.class, () -> QueueTokenVerifier.verify(TOKEN, secrets, QUEUE));
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), ex.getResponse().getStatus());
            assertTrue(ex.getMessage().contains("no issuer"));
        }
    }

    @Test
    public void testVerify_unsupportedIssuer_throwsBadRequest() {
        List<String> secrets = Arrays.asList("s");
        DecodedJWT jwt = mock(DecodedJWT.class);
        when(jwt.getIssuer()).thenReturn("somebody");
        try (MockedStatic<JwtUtils> jwtMock = Mockito.mockStatic(JwtUtils.class)) {
            jwtMock.when(() -> JwtUtils.verifyToken(TOKEN, "s")).thenReturn(jwt);
            WebApplicationException ex = assertThrows(WebApplicationException.class, () -> QueueTokenVerifier.verify(TOKEN, secrets, QUEUE));
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), ex.getResponse().getStatus());
            assertTrue(ex.getMessage().contains("unsupported issuer"));
        }
    }

    @Test
    public void testVerify_nullClaim_throwsBadRequest() {
        List<String> secrets = Arrays.asList("s");
        DecodedJWT jwt = mock(DecodedJWT.class);
        when(jwt.getIssuer()).thenReturn(QueueTokenVerifier.ISSUER_ADMIN);
        when(jwt.getClaim(QueueTokenVerifier.CLAIM_ALLOWED_QUEUES)).thenReturn(null);
        try (MockedStatic<JwtUtils> jwtMock = Mockito.mockStatic(JwtUtils.class)) {
            jwtMock.when(() -> JwtUtils.verifyToken(TOKEN, "s")).thenReturn(jwt);
            WebApplicationException ex = assertThrows(WebApplicationException.class, () -> QueueTokenVerifier.verify(TOKEN, secrets, QUEUE));
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), ex.getResponse().getStatus());
            assertTrue(ex.getMessage().contains("no claim"));
        }
    }

    @Test
    public void testVerify_claimIsNullClaim_throwsBadRequest() {
        List<String> secrets = Arrays.asList("s");
        DecodedJWT jwt = mock(DecodedJWT.class);
        Claim claim = mock(Claim.class);
        when(claim.isNull()).thenReturn(true);
        when(jwt.getIssuer()).thenReturn(QueueTokenVerifier.ISSUER_ADMIN);
        when(jwt.getClaim(QueueTokenVerifier.CLAIM_ALLOWED_QUEUES)).thenReturn(claim);
        try (MockedStatic<JwtUtils> jwtMock = Mockito.mockStatic(JwtUtils.class)) {
            jwtMock.when(() -> JwtUtils.verifyToken(TOKEN, "s")).thenReturn(jwt);
            WebApplicationException ex = assertThrows(WebApplicationException.class, () -> QueueTokenVerifier.verify(TOKEN, secrets, QUEUE));
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), ex.getResponse().getStatus());
            assertTrue(ex.getMessage().contains("no claim"));
        }
    }

    @Test
    public void testVerify_claimAsArrayThrows_thenAsListSucceeds_butDoesNotContainQueue() {
        List<String> secrets = Arrays.asList("s");
        DecodedJWT jwt = mock(DecodedJWT.class);
        Claim claim = mock(Claim.class);
        when(jwt.getIssuer()).thenReturn(QueueTokenVerifier.ISSUER_AIRFLOW);
        when(jwt.getClaim(QueueTokenVerifier.CLAIM_ALLOWED_QUEUES)).thenReturn(claim);
        when(claim.isNull()).thenReturn(false);
        when(claim.asArray(String.class)).thenThrow(new RuntimeException("bad array"));
        when(claim.asList(String.class)).thenReturn(Arrays.asList("otherQueue"));
        try (MockedStatic<JwtUtils> jwtMock = Mockito.mockStatic(JwtUtils.class)) {
            jwtMock.when(() -> JwtUtils.verifyToken(TOKEN, "s")).thenReturn(jwt);
            WebApplicationException ex = assertThrows(WebApplicationException.class, () -> QueueTokenVerifier.verify(TOKEN, secrets, QUEUE));
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), ex.getResponse().getStatus());
            assertTrue(ex.getMessage().contains("does not allow queue"));
        }
    }

    @Test
    public void testVerify_emptyArrayAndEmptyList_throwsBadRequest() {
        List<String> secrets = Arrays.asList("s");
        DecodedJWT jwt = mock(DecodedJWT.class);
        Claim claim = mock(Claim.class);
        when(jwt.getIssuer()).thenReturn(QueueTokenVerifier.ISSUER_ADMIN);
        when(jwt.getClaim(QueueTokenVerifier.CLAIM_ALLOWED_QUEUES)).thenReturn(claim);
        when(claim.isNull()).thenReturn(false);
        when(claim.asArray(String.class)).thenReturn(new String[0]);
        when(claim.asList(String.class)).thenReturn(Collections.emptyList());
        try (MockedStatic<JwtUtils> jwtMock = Mockito.mockStatic(JwtUtils.class)) {
            jwtMock.when(() -> JwtUtils.verifyToken(TOKEN, "s")).thenReturn(jwt);
            WebApplicationException ex = assertThrows(WebApplicationException.class, () -> QueueTokenVerifier.verify(TOKEN, secrets, QUEUE));
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), ex.getResponse().getStatus());
            assertTrue(ex.getMessage().contains("has no allowed queues"));
        }
    }

    @Test
    public void testVerify_success_whenAllowedQueuesContainsQueue() {
        List<String> secrets = Arrays.asList("s");
        DecodedJWT jwt = mock(DecodedJWT.class);
        Claim claim = mock(Claim.class);
        when(jwt.getIssuer()).thenReturn(QueueTokenVerifier.ISSUER_ADMIN);
        when(jwt.getClaim(QueueTokenVerifier.CLAIM_ALLOWED_QUEUES)).thenReturn(claim);
        when(claim.isNull()).thenReturn(false);
        when(claim.asArray(String.class)).thenReturn(new String[] { QUEUE, "other" });
        // asArray provided the queue, so asList shouldn't be called; but if it is, make it safe:
        when(claim.asList(String.class)).thenReturn(Arrays.asList(QUEUE, "other"));
        try (MockedStatic<JwtUtils> jwtMock = Mockito.mockStatic(JwtUtils.class)) {
            jwtMock.when(() -> JwtUtils.verifyToken(TOKEN, "s")).thenReturn(jwt);
            // should not throw
            assertDoesNotThrow(() -> QueueTokenVerifier.verify(TOKEN, secrets, QUEUE));
        }
    }
}
