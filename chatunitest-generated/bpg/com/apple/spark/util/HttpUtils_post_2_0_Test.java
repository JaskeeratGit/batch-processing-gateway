package com.apple.spark.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpUtils_post_2_0_Test {

    /**
     * Uses reflection to invoke the focal method:
     * HttpUtils.post(String url, String requestJson, String headerName, String headerValue)
     *
     * This test verifies that when the URL is well-formed but the underlying HTTP execution fails,
     * the class wraps the failure and the thrown RuntimeException message contains "POST on <url>".
     *
     * We avoid depending on an external server by using a port that will not succeed (port 0).
     * The HttpUtils implementation catches the underlying exception and throws a RuntimeException
     * with a message that includes the request method and the original URL; we assert that behavior.
     */
    @Test
    public void testPostDelegatesWithDefaultContentType_ThrowsRuntimeExceptionWithPostAndUrlInMessage() throws Exception {
        Method postMethod = HttpUtils.class.getMethod("post", String.class, String.class, String.class, String.class);
        postMethod.setAccessible(true);
        // well-formed URI; underlying execution should fail quickly
        String url = "http://127.0.0.1:0";
        String requestJson = "{\"k\":\"v\"}";
        InvocationTargetException ite = assertThrows(InvocationTargetException.class, () -> postMethod.invoke(null, url, requestJson, null, null));
        Throwable cause = ite.getCause();
        assertNotNull(cause, "Expected an underlying cause to be present");
        // The implementation wraps the underlying exception into a RuntimeException with message
        // "Failed to execute %s on %s" where %s is request.method() and url.
        // Validate that behavior.
        assertTrue(cause instanceof RuntimeException, "Expected a RuntimeException wrapping the underlying failure");
        String msg = cause.getMessage();
        assertNotNull(msg);
        assertTrue(msg.contains("POST on " + url), "Expected exception message to mention the HTTP method and the URL. Actual: " + msg);
    }

    /**
     * When the provided URL cannot be converted to a URI, the overloaded post method enters the catch
     * block where it uses the local 'request' variable to create the error message. Because 'request'
     * is still null in that scenario, accessing request.method() will produce a NullPointerException.
     *
     * This test ensures that behavior is present (i.e., a NullPointerException is thrown).
     */
    @Test
    public void testPostWithMalformedUrl_ThrowsNullPointerException() throws Exception {
        Method postMethod = HttpUtils.class.getMethod("post", String.class, String.class, String.class, String.class);
        postMethod.setAccessible(true);
        // invalid URI to force URISyntaxException in new URI(url)
        String malformedUrl = "ht tp://@@@";
        String requestJson = "{}";
        InvocationTargetException ite = assertThrows(InvocationTargetException.class, () -> postMethod.invoke(null, malformedUrl, requestJson, "X-Name", "X-Value"));
        Throwable cause = ite.getCause();
        assertNotNull(cause);
        // Because the catch block tries to use request.method() while request is still null,
        // a NullPointerException is expected to be thrown (as the original catch's behavior).
        assertTrue(cause instanceof NullPointerException, "Expected a NullPointerException when URL is malformed and request is null. Actual: " + cause.getClass());
    }

    /**
     * Similar to the malformed URL test, verify behavior when headerName is empty. Use a malformed
     * URL so the method fails before executeHttpRequest is invoked; the catch block will try to
     * reference the uninitialized request and throw NullPointerException.
     */
    @Test
    public void testPostWithEmptyHeaderName_ThrowsNullPointerException() throws Exception {
        Method postMethod = HttpUtils.class.getMethod("post", String.class, String.class, String.class, String.class);
        postMethod.setAccessible(true);
        // causes URISyntaxException
        String malformedUrl = "bad_uri";
        String requestJson = "{\"a\":1}";
        InvocationTargetException ite = assertThrows(InvocationTargetException.class, () -> postMethod.invoke(null, malformedUrl, requestJson, "", "value"));
        Throwable cause = ite.getCause();
        assertNotNull(cause);
        assertTrue(cause instanceof NullPointerException, "Expected NullPointerException when request is uninitialized in catch. Actual: " + cause.getClass());
    }
}
