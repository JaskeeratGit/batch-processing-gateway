package com.apple.spark.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpUtils_post_4_0_Test_testPostReturnsString_whenResponseIsJsonString {

    // Simple POJO for JSON parsing tests
    public static class Person {

        public String name;

        public int age;

        // No-arg constructor is required by Jackson
        public Person() {
        }
    }

    @Test
    public void testPostReturnsString_whenResponseIsJsonString() throws Exception {
        String json = "\"hello world\"";
        InputStream stream = new ByteArrayInputStream(json.getBytes());
        // Call the focal public method. It will internally call post(url, stream, headerName, headerValue)
        // and then parseJson(...) to convert the JSON into the requested class.
        String result = HttpUtils.post("http://example", stream, "X-Header", "val", String.class);
        assertEquals("hello world", result);
        // Additionally, use reflection to call the private parseJson(String, Class) method directly
        Method parseJson = HttpUtils.class.getDeclaredMethod("parseJson", String.class, Class.class);
        parseJson.setAccessible(true);
        @SuppressWarnings("unchecked")
        String reflected = (String) parseJson.invoke(null, json, String.class);
        assertEquals("hello world", reflected);
    }

}
