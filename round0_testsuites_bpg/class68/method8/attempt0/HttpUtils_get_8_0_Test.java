package com.apple.spark.util;

import static org.mockito.ArgumentMatchers.any;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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

public class HttpUtils_get_8_0_Test {

    public static class Person {

        public String name;

        public int age;

        public Person() {
        }

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Person person = (Person) o;
            return age == person.age && (name == null ? person.name == null : name.equals(person.name));
        }

        @Override
        public int hashCode() {
            int result = (name != null) ? name.hashCode() : 0;
            result = 31 * result + age;
            return result;
        }
    }

    @Test
    public void testGet_success_withHeader() throws Exception {
        String json = "{\"name\":\"Alice\",\"age\":30}";
        try (MockedStatic<HttpClient> httpClientStatic = Mockito.mockStatic(HttpClient.class)) {
            HttpClient.Builder mockBuilder = mock(HttpClient.Builder.class);
            HttpClient mockClient = mock(HttpClient.class);
            @SuppressWarnings("unchecked")
            HttpResponse<String> mockResponse = mock(HttpResponse.class);
            httpClientStatic.when(HttpClient::newBuilder).thenReturn(mockBuilder);
            when(mockBuilder.build()).thenReturn(mockClient);
            when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);
            when(mockResponse.body()).thenReturn(json);
            Person result = HttpUtils.get("http://example.com/test", "X-Test", "value", Person.class);
            assertNotNull(result);
            assertEquals(new Person("Alice", 30), result);
        }
    }

    @Test
    public void testGet_success_withoutHeader() throws Exception {
        String json = "{\"name\":\"Bob\",\"age\":25}";
        try (MockedStatic<HttpClient> httpClientStatic = Mockito.mockStatic(HttpClient.class)) {
            HttpClient.Builder mockBuilder = mock(HttpClient.Builder.class);
            HttpClient mockClient = mock(HttpClient.class);
            @SuppressWarnings("unchecked")
            HttpResponse<String> mockResponse = mock(HttpResponse.class);
            httpClientStatic.when(HttpClient::newBuilder).thenReturn(mockBuilder);
            when(mockBuilder.build()).thenReturn(mockClient);
            when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);
            when(mockResponse.body()).thenReturn(json);
            // headerName is null -> branch where header not added
            Person result = HttpUtils.get("http://example.com/noheader", null, "value", Person.class);
            assertNotNull(result);
            assertEquals(new Person("Bob", 25), result);
            // empty header name -> also header not added
            Person result2 = HttpUtils.get("http://example.com/noheader", "", "value", Person.class);
            assertNotNull(result2);
            assertEquals(new Person("Bob", 25), result2);
        }
    }

    @Test
    public void testGet_httpClientThrows_runtimeExceptionPropagated() throws Exception {
        try (MockedStatic<HttpClient> httpClientStatic = Mockito.mockStatic(HttpClient.class)) {
            HttpClient.Builder mockBuilder = mock(HttpClient.Builder.class);
            HttpClient mockClient = mock(HttpClient.class);
            httpClientStatic.when(HttpClient::newBuilder).thenReturn(mockBuilder);
            when(mockBuilder.build()).thenReturn(mockClient);
            when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenThrow(new IOException("network down"));
            assertThrows(RuntimeException.class, () -> HttpUtils.get("http://example.com/fail", "H", "v", Person.class));
        }
    }

    @Test
    public void testParseJson_privateMethod_viaReflection() throws Exception {
        // prepare JSON and expected object
        String json = "{\"name\":\"Carol\",\"age\":40}";
        // reflectively find a method named parseJson that accepts (String, Class)
        Method parseJsonMethod = null;
        for (Method m : HttpUtils.class.getDeclaredMethods()) {
            if (m.getName().equals("parseJson")) {
                Class<?>[] params = m.getParameterTypes();
                if (params.length == 2 && params[0] == String.class && params[1] == Class.class) {
                    parseJsonMethod = m;
                    break;
                }
            }
        }
        assertNotNull(parseJsonMethod, "parseJson(String, Class) method must exist in HttpUtils");
        parseJsonMethod.setAccessible(true);
        Object parsed = parseJsonMethod.invoke(null, json, Person.class);
        assertNotNull(parsed);
        assertTrue(parsed instanceof Person);
        assertEquals(new Person("Carol", 40), parsed);
    }
}
