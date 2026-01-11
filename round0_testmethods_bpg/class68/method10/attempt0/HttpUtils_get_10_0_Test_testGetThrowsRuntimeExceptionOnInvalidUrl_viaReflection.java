package com.apple.spark.util;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
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

public class HttpUtils_get_10_0_Test_testGetThrowsRuntimeExceptionOnInvalidUrl_viaReflection {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }



    @Test
    void testGetThrowsRuntimeExceptionOnInvalidUrl_viaReflection() throws Exception {
        String badUrl = "://bad";
        Method oneArgGet = HttpUtils.class.getDeclaredMethod("get", String.class);
        oneArgGet.setAccessible(true);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            try {
                oneArgGet.invoke(null, badUrl);
            } catch (java.lang.reflect.InvocationTargetException ite) {
                // unwrap to throw the underlying exception so assertThrows can catch the RuntimeException
                throw (RuntimeException) ite.getCause();
            }
        });
        // message should include the url
        assertEquals(String.format("Failed to get from %s", badUrl), ex.getMessage());
        // cause should be a URISyntaxException (or subclass), verify it
        assertTrue(ex.getCause() instanceof URISyntaxException);
    }
}
