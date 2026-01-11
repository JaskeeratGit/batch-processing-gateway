package com.apple.spark.util;

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

public class HttpUtils_post_0_0_Test {

    @Test
    void postDelegatesWithApplicationJson() throws Exception {
        String url = "http://example.com/api";
        String requestJson = "{\"key\":\"value\"}";
        String headerName = "X-Custom-Header";
        String headerValue = "header-value";
        try (MockedStatic<HttpUtils> mocked = Mockito.mockStatic(HttpUtils.class, Mockito.CALLS_REAL_METHODS)) {
            // stub the 6-arg overload to return a sentinel value when contentType is "application/json"
            mocked.when(() -> HttpUtils.post(url, requestJson, "application/json", headerName, headerValue, String.class)).thenReturn("SENTINEL");
            // call the 5-arg overload (the focal method) which should delegate to the 6-arg overload
            String result = HttpUtils.post(url, requestJson, headerName, headerValue, String.class);
            assertEquals("SENTINEL", result);
            // verify the 6-arg overload was invoked with the expected content-type
            mocked.verify(() -> HttpUtils.post(url, requestJson, "application/json", headerName, headerValue, String.class));
            // also invoke the focal method via reflection (demonstrates reflective invocation)
            Method focal = HttpUtils.class.getDeclaredMethod("post", String.class, String.class, String.class, String.class, Class.class);
            focal.setAccessible(true);
            Object reflectiveResult = focal.invoke(null, url, requestJson, headerName, headerValue, String.class);
            assertEquals("SENTINEL", reflectiveResult);
        }
    }

    @Test
    void postDelegatesWithNullHeadersAndDifferentReturnType() {
        String url = "http://example.com/other";
        String requestJson = "{}";
        String headerName = null;
        String headerValue = null;
        try (MockedStatic<HttpUtils> mocked = Mockito.mockStatic(HttpUtils.class, Mockito.CALLS_REAL_METHODS)) {
            // stub for Integer.class return type and null headers
            mocked.when(() -> HttpUtils.post(url, requestJson, "application/json", headerName, headerValue, Integer.class)).thenReturn(42);
            Integer result = HttpUtils.post(url, requestJson, headerName, headerValue, Integer.class);
            assertEquals(42, result.intValue());
            mocked.verify(() -> HttpUtils.post(url, requestJson, "application/json", headerName, headerValue, Integer.class));
        }
    }
}
