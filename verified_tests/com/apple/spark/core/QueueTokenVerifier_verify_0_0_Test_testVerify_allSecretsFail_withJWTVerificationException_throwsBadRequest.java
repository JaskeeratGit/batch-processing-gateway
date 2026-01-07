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

public class QueueTokenVerifier_verify_0_0_Test_testVerify_allSecretsFail_withJWTVerificationException_throwsBadRequest {

    private final String TOKEN = "dummy-token";

    private final String QUEUE = "targetQueue";




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








}
