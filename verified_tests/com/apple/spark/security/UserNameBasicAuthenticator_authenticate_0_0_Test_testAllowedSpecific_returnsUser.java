package com.apple.spark.security;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.basic.BasicCredentials;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import io.dropwizard.auth.Authenticator;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserNameBasicAuthenticator_authenticate_0_0_Test_testAllowedSpecific_returnsUser {



    @Test
    public void testAllowedSpecific_returnsUser() throws AuthenticationException {
        UserNameBasicAuthenticator auth = new UserNameBasicAuthenticator(Collections.singletonList("alice"), Collections.emptyList());
        BasicCredentials creds = new BasicCredentials("alice", "pw");
        Optional<User> result = auth.authenticate(creds);
        assertTrue(result.isPresent(), "Specific allowed user should be authenticated");
        assertEquals("com.apple.spark.security.User", result.get().getClass().getName());
    }


}
