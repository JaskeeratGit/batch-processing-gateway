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

public class HttpUtils_post_4_0_Test_testPostParsesPojo_whenResponseIsJsonObject {

    // Simple POJO for JSON parsing tests
    public static class Person {

        public String name;

        public int age;

        // No-arg constructor is required by Jackson
        public Person() {
        }
    }


    @Test
    public void testPostParsesPojo_whenResponseIsJsonObject() throws Exception {
        String json = "{\"name\":\"Alice\",\"age\":30}";
        InputStream stream = new ByteArrayInputStream(json.getBytes());
        Person person = HttpUtils.post("http://example", stream, "Header", "Value", Person.class);
        assertEquals("Alice", person.name);
        assertEquals(30, person.age);
        // Also verify using the private parseJson via reflection gives the same result
        Method parseJson = HttpUtils.class.getDeclaredMethod("parseJson", String.class, Class.class);
        parseJson.setAccessible(true);
        @SuppressWarnings("unchecked")
        Person reflected = (Person) parseJson.invoke(null, json, Person.class);
        assertEquals("Alice", reflected.name);
        assertEquals(30, reflected.age);
    }
}
