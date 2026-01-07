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
}
