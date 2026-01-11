package com.apple.spark.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpUtils_post_5_0_Test_testPostParsesJsonSuccessfully {

    // Simple DTO for JSON mapping in tests
    public static class TestDto {

        private String name;

        private int value;

        public TestDto() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

    @Test
    public void testPostParsesJsonSuccessfully() {
        String url = "http://example.com";
        InputStream stream = new ByteArrayInputStream(new byte[0]);
        String headerName = "X-Test";
        String headerValue = "val";
        long contentLength = 0L;
        String json = "{\"name\":\"Alice\",\"value\":42}";
        try (MockedStatic<HttpUtils> mocked = Mockito.mockStatic(HttpUtils.class)) {
            // Stub the 5-arg post to return the JSON string
            mocked.when(() -> HttpUtils.post(url, stream, headerName, headerValue, contentLength)).thenReturn(json);
            // For the 6-arg method under test, call the real method (so it will call the stubbed 5-arg method)
            mocked.when(() -> HttpUtils.post(url, stream, headerName, headerValue, contentLength, TestDto.class)).thenCallRealMethod();
            TestDto result = HttpUtils.post(url, stream, headerName, headerValue, contentLength, TestDto.class);
            assertNotNull(result);
            assertEquals("Alice", result.getName());
            assertEquals(42, result.getValue());
        }
    }


}
