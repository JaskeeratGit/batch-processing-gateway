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

public class JwtUtils_createToken_1_0_Test_testCreateToken_wrongSecret_verificationFails {

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
    public void testCreateToken_wrongSecret_verificationFails() {
        String goodSecret = "good-secret";
        String badSecret = "bad-secret";
        String issuer = "issuer";
        String subject = "subject";
        String claimName = "c";
        List<String> claimItems = Arrays.asList("one");
        String token = invokeCreateTokenReflectively(goodSecret, issuer, subject, claimName, claimItems);
        assertNotNull(token);
        Algorithm wrongAlg = Algorithm.HMAC256(badSecret);
        JWTVerifier wrongVerifier = JWT.require(wrongAlg).withIssuer(issuer).withSubject(subject).build();
        assertThrows(JWTVerificationException.class, () -> wrongVerifier.verify(token));
    }

}
