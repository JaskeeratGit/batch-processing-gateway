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

public class JwtUtils_verifyToken_2_0_Test {

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

    @Test
    public void testVerifyTokenInvalidSignatureThrows() {
        String secretSigned = "originalSecret";
        String secretVerify = "differentSecret";
        // token signed with originalSecret
        String token = JWT.create().withSubject("bob").sign(Algorithm.HMAC256(secretSigned));
        assertThrows(JWTVerificationException.class, () -> JwtUtils.verifyToken(token, secretVerify), "Verifying with the wrong secret should throw JWTVerificationException");
    }

    @Test
    public void testVerifyTokenNullSecretThrowsIllegalArgument() {
        String secret = null;
        // create any token string (it won't be verified since secret is null and Algorithm.HMAC256 should fail)
        // short dummy-looking token
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
        assertThrows(IllegalArgumentException.class, () -> JwtUtils.verifyToken(token, secret), "Passing a null secret should result in IllegalArgumentException from Algorithm.HMAC256");
    }

    @Test
    public void testVerifyTokenNullTokenThrows() {
        String secret = "someSecret";
        // Behavior when token is null depends on underlying verifier; ensure an exception is thrown
        assertThrows(Exception.class, () -> JwtUtils.verifyToken(null, secret), "Passing a null token should throw an exception from the verifier");
    }
}
