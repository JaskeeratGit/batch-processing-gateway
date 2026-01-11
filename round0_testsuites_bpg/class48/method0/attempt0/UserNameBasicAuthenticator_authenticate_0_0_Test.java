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

public class UserNameBasicAuthenticator_authenticate_0_0_Test {

    @Test
    public void testBlockedUser_returnsEmpty() throws AuthenticationException {
        UserNameBasicAuthenticator auth = new UserNameBasicAuthenticator(Collections.emptyList(), Collections.singletonList("blockedUser"));
        BasicCredentials creds = new BasicCredentials("blockedUser", "pw");
        Optional<User> result = auth.authenticate(creds);
        assertFalse(result.isPresent(), "Blocked user should not be authenticated");
    }

    @Test
    public void testAllowedWildcard_returnsUser() throws AuthenticationException {
        UserNameBasicAuthenticator auth = new UserNameBasicAuthenticator(Collections.singletonList("*"), Collections.emptyList());
        BasicCredentials creds = new BasicCredentials("anyUser", "pw");
        Optional<User> result = auth.authenticate(creds);
        assertTrue(result.isPresent(), "User should be authenticated when '*' is allowed");
        assertEquals("com.apple.spark.security.User", result.get().getClass().getName());
    }

    @Test
    public void testAllowedSpecific_returnsUser() throws AuthenticationException {
        UserNameBasicAuthenticator auth = new UserNameBasicAuthenticator(Collections.singletonList("alice"), Collections.emptyList());
        BasicCredentials creds = new BasicCredentials("alice", "pw");
        Optional<User> result = auth.authenticate(creds);
        assertTrue(result.isPresent(), "Specific allowed user should be authenticated");
        assertEquals("com.apple.spark.security.User", result.get().getClass().getName());
    }

    @Test
    public void testNotAllowed_returnsEmpty() throws AuthenticationException {
        UserNameBasicAuthenticator auth = new UserNameBasicAuthenticator(Collections.singletonList("alice"), Collections.emptyList());
        BasicCredentials creds = new BasicCredentials("bob", "pw");
        Optional<User> result = auth.authenticate(creds);
        assertFalse(result.isPresent(), "Non-allowed user should not be authenticated");
    }

    @Test
    public void testBlockedOverridesWildcard_returnsEmpty() throws Exception {
        UserNameBasicAuthenticator auth = new UserNameBasicAuthenticator(Arrays.asList("*"), Arrays.asList("bob"));
        BasicCredentials creds = new BasicCredentials("bob", "pw");
        // invoke via reflection to demonstrate reflective invocation (method is public here)
        Method m = UserNameBasicAuthenticator.class.getDeclaredMethod("authenticate", BasicCredentials.class);
        m.setAccessible(true);
        Object invoked = m.invoke(auth, creds);
        assertTrue(invoked instanceof Optional, "authenticate should return an Optional");
        @SuppressWarnings("unchecked")
        Optional<User> result = (Optional<User>) invoked;
        assertFalse(result.isPresent(), "Blocked user should not be authenticated even if wildcard is allowed");
    }
}
