package com.apple.spark.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.List;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.auth0.jwt.interfaces.JWTVerifier;

public class JwtUtils_verifyToken_2_0_Test_testVerifyTokenValid {

    @Test
    public void testVerifyTokenValid() {
        String secret = "verySecretKey123!";
        // create a token signed with the same secret
        String token = JWT.create().withSubject("alice").withClaim("roles", List.of("admin", "user")).sign(Algorithm.HMAC256(secret));
        DecodedJWT decoded = JwtUtils.verifyToken(token, secret);
        assertNotNull(decoded, "DecodedJWT should not be null for a valid token");
        assertEquals("alice", decoded.getSubject(), "Subject should match the one in the token");
        List<String> roles = decoded.getClaim("roles").asList(String.class);
        assertNotNull(roles, "Roles claim should not be null");
        assertEquals(2, roles.size(), "There should be two roles");
        assertTrue(roles.contains("admin") && roles.contains("user"), "Roles should contain admin and user");
    }



}
