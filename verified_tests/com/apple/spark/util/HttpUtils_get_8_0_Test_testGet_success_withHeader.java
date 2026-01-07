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

public class HttpUtils_get_8_0_Test_testGet_success_withHeader {

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



}
