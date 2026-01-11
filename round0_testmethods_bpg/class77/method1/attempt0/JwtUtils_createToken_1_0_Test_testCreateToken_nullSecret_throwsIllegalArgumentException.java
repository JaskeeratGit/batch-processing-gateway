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

public class JwtUtils_createToken_1_0_Test_testCreateToken_nullSecret_throwsIllegalArgumentException {

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
    public void testCreateToken_nullSecret_throwsIllegalArgumentException() {
        String secret = null;
        String issuer = "iss";
        String subject = "sub";
        String claimName = "claim";
        List<String> claimItems = Arrays.asList("a");
        // Algorithm.HMAC256(null) is expected to throw IllegalArgumentException; reflective invocation
        // unwraps the cause so assertThrows can see it.
        assertThrows(IllegalArgumentException.class, () -> invokeCreateTokenReflectively(secret, issuer, subject, claimName, claimItems));
    }
}
