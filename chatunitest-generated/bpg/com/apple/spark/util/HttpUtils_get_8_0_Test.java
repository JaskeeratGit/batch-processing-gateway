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
