package com.apple.spark.util;

import org.mockito.junit.jupiter.MockitoExtension;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;

public class HttpUtils_post_0_0_Test {

    @Test
    void postDelegatesWithApplicationJson() {
        String url = "http://example.com/api";
        String requestJson = "{\"key\":\"value\"}";
        String headerName = "X-Custom-Header";
        String headerValue = "header-value";
        String expected = "expected-response";
        try {
            MockedStatic<HttpUtils> mocked = Mockito.mockStatic(HttpUtils.class);
            try (mocked) {
                // Stub the overload that accepts contentType to return expected
                mocked.when(() -> HttpUtils.post(url, requestJson, "application/json", headerName, headerValue, String.class)).thenReturn(expected);
                // Let the focal (5-arg) method execute its real implementation (which delegates)
                mocked.when(() -> HttpUtils.post(url, requestJson, headerName, headerValue, String.class)).thenCallRealMethod();
                Object result = HttpUtils.post(url, requestJson, headerName, headerValue, String.class);
                assertEquals(expected, result);
            }
        } catch (IllegalStateException e) {
            // Static mocking not supported in current runtime (mockito-inline not available).
            // Skip the test gracefully.
            Assumptions.assumeTrue(false, "Static mocking not supported in this environment: " + e.getMessage());
        }
    }

    @Test
    void postDelegatesWithNullHeadersAndDifferentReturnType() {
        String url = "http://example.com/other";
        String requestJson = "{}";
        String headerName = null;
        String headerValue = null;
        Integer expected = 123;
        try {
            MockedStatic<HttpUtils> mocked = Mockito.mockStatic(HttpUtils.class);
            try (mocked) {
                // Stub the overload that accepts contentType to return expected Integer
                mocked.when(() -> HttpUtils.post(url, requestJson, "application/json", headerName, headerValue, Integer.class)).thenReturn(expected);
                // Let the focal (5-arg) method execute its real implementation (which delegates)
                mocked.when(() -> HttpUtils.post(url, requestJson, headerName, headerValue, Integer.class)).thenCallRealMethod();
                Integer result = HttpUtils.post(url, requestJson, headerName, headerValue, Integer.class);
                assertEquals(expected, result);
            }
        } catch (IllegalStateException e) {
            // Static mocking not supported in current runtime (mockito-inline not available).
            // Skip the test gracefully.
            Assumptions.assumeTrue(false, "Static mocking not supported in this environment: " + e.getMessage());
        }
    }
}
