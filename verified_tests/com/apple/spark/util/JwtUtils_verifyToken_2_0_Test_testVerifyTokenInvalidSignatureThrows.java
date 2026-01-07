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

public class JwtUtils_verifyToken_2_0_Test_testVerifyTokenInvalidSignatureThrows {


    @Test
    public void testVerifyTokenInvalidSignatureThrows() {
        String secretSigned = "originalSecret";
        String secretVerify = "differentSecret";
        // token signed with originalSecret
        String token = JWT.create().withSubject("bob").sign(Algorithm.HMAC256(secretSigned));
        assertThrows(JWTVerificationException.class, () -> JwtUtils.verifyToken(token, secretVerify), "Verifying with the wrong secret should throw JWTVerificationException");
    }


}
