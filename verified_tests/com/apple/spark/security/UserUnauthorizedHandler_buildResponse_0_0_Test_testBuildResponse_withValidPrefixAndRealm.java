package com.apple.spark.security;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.lang.reflect.Method;
import java.util.Map;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.auth.UnauthorizedHandler;

public class UserUnauthorizedHandler_buildResponse_0_0_Test_testBuildResponse_withValidPrefixAndRealm {

    @Test
    public void testBuildResponse_withValidPrefixAndRealm() throws Exception {
        UserUnauthorizedHandler handler = new UserUnauthorizedHandler();
        Method buildResponse = UserUnauthorizedHandler.class.getDeclaredMethod("buildResponse", String.class, String.class);
        buildResponse.setAccessible(true);
        Response resp = (Response) buildResponse.invoke(handler, "Bearer", "myRealm");
        assertNotNull(resp, "Response should not be null");
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), resp.getStatus(), "Status should be 401");
        String expectedHeader = String.format("%s realm=\"%s\"", "Bearer", "myRealm");
        assertEquals(expectedHeader, resp.getHeaderString(HttpHeaders.WWW_AUTHENTICATE), "WWW-Authenticate header mismatch");
        Object entity = resp.getEntity();
        assertNotNull(entity, "Entity should not be null");
        assertTrue(entity instanceof Map, "Entity should be a Map");
        @SuppressWarnings("unchecked")
        Map<String, String> map = (Map<String, String>) entity;
        assertEquals("401", map.get("code"));
        assertEquals("HTTP 401 Unauthorized.", map.get("message"));
    }

}
