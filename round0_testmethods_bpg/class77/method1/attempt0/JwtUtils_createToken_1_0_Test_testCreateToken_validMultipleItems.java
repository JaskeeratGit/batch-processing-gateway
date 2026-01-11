package com.apple.spark.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

public class JwtUtils_createToken_1_0_Test_testCreateToken_validMultipleItems {

    /**
     * Invoke JwtUtils.createToken reflectively and unwrap reflection exceptions so tests can assert
     * on underlying exceptions.
     */
    private static String invokeCreateTokenReflectively(String secret, String issuer, String subject, String claimName, List<String> claimItems) {
        try {
            Class<?> cls = Class.forName("com.apple.spark.util.JwtUtils");
            Method m = cls.getMethod("createToken", String.class, String.class, String.class, String.class, List.class);
            Object res = m.invoke(null, secret, issuer, subject, claimName, claimItems);
            return (String) res;
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new RuntimeException(cause);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testCreateToken_validMultipleItems() {
        String secret = "my-very-secret";
        String issuer = "test-issuer";
        String subject = "test-subject";
        String claimName = "roles";
        List<String> claimItems = Arrays.asList("admin", "user");
        String token = invokeCreateTokenReflectively(secret, issuer, subject, claimName, claimItems);
        assertNotNull(token);
        // verify signature and claims
        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTVerifier verifier = JWT.require(algorithm).withIssuer(issuer).withSubject(subject).build();
        DecodedJWT decoded = verifier.verify(token);
        assertEquals(issuer, decoded.getIssuer());
        assertEquals(subject, decoded.getSubject());
        List<String> decodedClaim = decoded.getClaim(claimName).asList(String.class);
        assertEquals(claimItems, decodedClaim);
    }



}
