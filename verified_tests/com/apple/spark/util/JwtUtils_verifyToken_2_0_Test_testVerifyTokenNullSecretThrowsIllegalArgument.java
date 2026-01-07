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

public class JwtUtils_verifyToken_2_0_Test_testVerifyTokenNullSecretThrowsIllegalArgument {



    @Test
    public void testVerifyTokenNullSecretThrowsIllegalArgument() {
        String secret = null;
        // create any token string (it won't be verified since secret is null and Algorithm.HMAC256 should fail)
        // short dummy-looking token
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
        assertThrows(IllegalArgumentException.class, () -> JwtUtils.verifyToken(token, secret), "Passing a null secret should result in IllegalArgumentException from Algorithm.HMAC256");
    }

}
