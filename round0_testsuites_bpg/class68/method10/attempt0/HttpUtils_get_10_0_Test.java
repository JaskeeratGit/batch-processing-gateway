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

public class HttpUtils_get_10_0_Test {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    @Test
    void testGetReturnsBody_usingOneArgGet_viaReflection() throws Exception {
        // start simple local HTTP server that returns fixed body
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/test", new HttpHandler() {

            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String resp = "hello-world";
                exchange.sendResponseHeaders(200, resp.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(resp.getBytes());
                }
                exchange.close();
            }
        });
        server.start();
        int port = server.getAddress().getPort();
        String url = "http://localhost:" + port + "/test";
        // Invoke public static String get(String) reflectively
        Method oneArgGet = HttpUtils.class.getDeclaredMethod("get", String.class);
        oneArgGet.setAccessible(true);
        Object result = oneArgGet.invoke(null, url);
        assertEquals("hello-world", result);
    }

    @Test
    void testGetSendsHeader_whenProvided_viaReflectionToThreeArgMethod() throws Exception {
        // start server that echoes a header value back in the response body
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/hdr", new HttpHandler() {

            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String headerVal = exchange.getRequestHeaders().getFirst("X-Test-Header");
                if (headerVal == null) {
                    headerVal = "MISSING";
                }
                exchange.sendResponseHeaders(200, headerVal.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(headerVal.getBytes());
                }
                exchange.close();
            }
        });
        server.start();
        int port = server.getAddress().getPort();
        String url = "http://localhost:" + port + "/hdr";
        String headerName = "X-Test-Header";
        String headerValue = "the-value";
        // Invoke public static String get(String, String, String) reflectively
        Method threeArgGet = HttpUtils.class.getDeclaredMethod("get", String.class, String.class, String.class);
        threeArgGet.setAccessible(true);
        Object result = threeArgGet.invoke(null, url, headerName, headerValue);
        assertEquals(headerValue, result);
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
