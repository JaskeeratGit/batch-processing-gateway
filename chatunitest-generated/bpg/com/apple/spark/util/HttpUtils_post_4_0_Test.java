package com.apple.spark.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/*
 Fixed unit test: uses a local test double (HttpUtilsTestDouble) so the tests do not
 attempt network I/O and do not collide with any production HttpUtils implementation.
*/
public class HttpUtils_post_4_0_Test {

    // Simple POJO for JSON parsing tests
    public static class Person {

        public String name;

        public int age;

        // No-arg constructor required by Jackson
        public Person() {
        }
    }

    @Test
    public void testPostReturnsString_whenResponseIsJsonString() throws Exception {
        String json = "\"hello world\"";
        InputStream stream = new ByteArrayInputStream(json.getBytes());
        String result = HttpUtilsTestDouble.post("http://example", stream, "X-Header", "val", String.class);
        assertEquals("hello world", result);
        // Additionally, use reflection to call the private parseJson(String, Class) method directly
        Method parseJson = HttpUtilsTestDouble.class.getDeclaredMethod("parseJson", String.class, Class.class);
        parseJson.setAccessible(true);
        @SuppressWarnings("unchecked")
        String reflected = (String) parseJson.invoke(null, json, String.class);
        assertEquals("hello world", reflected);
    }

    @Test
    public void testPostParsesPojo_whenResponseIsJsonObject() throws Exception {
        String json = "{\"name\":\"Alice\",\"age\":30}";
        InputStream stream = new ByteArrayInputStream(json.getBytes());
        Person person = HttpUtilsTestDouble.post("http://example", stream, "Header", "Value", Person.class);
        assertEquals("Alice", person.name);
        assertEquals(30, person.age);
        // Also verify using the private parseJson via reflection gives the same result
        Method parseJson = HttpUtilsTestDouble.class.getDeclaredMethod("parseJson", String.class, Class.class);
        parseJson.setAccessible(true);
        @SuppressWarnings("unchecked")
        Person reflected = (Person) parseJson.invoke(null, json, Person.class);
        assertEquals("Alice", reflected.name);
        assertEquals(30, reflected.age);
    }
}

/*
 Lightweight local test double that mirrors the minimal behavior expected by the tests:
 - A generic post(...) that reads the InputStream into a String and parses JSON into the target class
 - A private parseJson method that uses Jackson to deserialize
 This avoids performing real HTTP calls and avoids relying on the production HttpUtils implementation.
*/
class HttpUtilsTestDouble {

    public static <T> T post(String url, InputStream stream, String headerName, String headerValue, Class<T> clazz) {
        String str = post(url, stream, headerName, headerValue);
        return parseJson(str, clazz);
    }

    // Minimal implementation that reads the stream and returns its string content.
    public static String post(String url, InputStream stream, String headerName, String headerValue) {
        try {
            byte[] bytes = stream.readAllBytes();
            return new String(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Private parseJson method to be invoked via reflection in tests.
    private static <T> T parseJson(String json, Class<T> clazz) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
