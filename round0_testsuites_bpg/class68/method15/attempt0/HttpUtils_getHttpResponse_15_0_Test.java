package com.apple.spark.util;

import com.apple.spark.util.HttpUtils;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import javax.net.ssl.SSLSession;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;

public class HttpUtils_getHttpResponse_15_0_Test {

    // Helper simple HttpResponse implementation for reflection tests
    static class SimpleHttpResponse<T> implements HttpResponse<T> {

        private final int code;

        private final T body;

        private final HttpRequest request;

        SimpleHttpResponse(int code, T body) {
            this.code = code;
            this.body = body;
            this.request = HttpRequest.newBuilder().uri(URI.create("http://localhost/")).GET().build();
        }

        @Override
        public int statusCode() {
            return code;
        }

        @Override
        public HttpRequest request() {
            return request;
        }

        @Override
        public Optional<HttpResponse<T>> previousResponse() {
            return Optional.empty();
        }

        @Override
        public HttpHeaders headers() {
            return HttpHeaders.of(Collections.emptyMap(), (k, v) -> true);
        }

        @Override
        public T body() {
            return body;
        }

        @Override
        public Optional<SSLSession> sslSession() {
            return Optional.empty();
        }

        @Override
        public URI uri() {
            return request.uri();
        }

        @Override
        public HttpClient.Version version() {
            return HttpClient.Version.HTTP_1_1;
        }
    }

    @Test
    public void testGetHttpResponse_successWithoutHeader() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        AtomicReference<String> capturedHeader = new AtomicReference<>();
        server.createContext("/test", (HttpExchange exchange) -> {
            try (OutputStream os = exchange.getResponseBody()) {
                capturedHeader.set(exchange.getRequestHeaders().getFirst("X-Test"));
                byte[] resp = "ok".getBytes();
                exchange.sendResponseHeaders(200, resp.length);
                os.write(resp);
            } finally {
                exchange.close();
            }
        });
        server.start();
        try {
            int port = server.getAddress().getPort();
            String url = "http://127.0.0.1:" + port + "/test";
            HttpResponse response = HttpUtils.getHttpResponse(url, "", "");
            assertNotNull(response);
            assertEquals(200, response.statusCode());
            assertEquals("ok", response.body());
            // header should be null because headerName was empty
            assertNull(capturedHeader.get());
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void testGetHttpResponse_withHeader() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        AtomicReference<String> capturedHeader = new AtomicReference<>();
        server.createContext("/test", (HttpExchange exchange) -> {
            try (OutputStream os = exchange.getResponseBody()) {
                capturedHeader.set(exchange.getRequestHeaders().getFirst("X-Test"));
                byte[] resp = "ok".getBytes();
                exchange.sendResponseHeaders(200, resp.length);
                os.write(resp);
            } finally {
                exchange.close();
            }
        });
        server.start();
        try {
            int port = server.getAddress().getPort();
            String url = "http://127.0.0.1:" + port + "/test";
            HttpResponse response = HttpUtils.getHttpResponse(url, "X-Test", "my-value");
            assertNotNull(response);
            assertEquals(200, response.statusCode());
            assertEquals("ok", response.body());
            assertEquals("my-value", capturedHeader.get());
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void testGetHttpResponse_serverErrorThrowsRuntimeException() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/err", (HttpExchange exchange) -> {
            try (OutputStream os = exchange.getResponseBody()) {
                byte[] resp = "error".getBytes();
                exchange.sendResponseHeaders(500, resp.length);
                os.write(resp);
            } finally {
                exchange.close();
            }
        });
        server.start();
        try {
            int port = server.getAddress().getPort();
            String url = "http://127.0.0.1:" + port + "/err";
            RuntimeException ex = assertThrows(RuntimeException.class, () -> {
                HttpUtils.getHttpResponse(url, null, null);
            });
            // The RuntimeException is expected to be from checkResponseOK or rethrown; message may vary.
            assertNotNull(ex);
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void testGetHttpResponse_connectionFailureWrapped() {
        // Use a port that is very likely closed to cause a connection failure (send will throw)
        String url = "http://127.0.0.1:1/some";
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            HttpUtils.getHttpResponse(url, null, null);
        });
        assertTrue(ex.getMessage().contains(url));
        assertNotNull(ex.getCause());
    }

    @Test
    public void testCheckResponseOK_reflection() throws Exception {
        // Obtain private method checkResponseOK(String, HttpResponse)
        Method m = HttpUtils.class.getDeclaredMethod("checkResponseOK", String.class, HttpResponse.class);
        m.setAccessible(true);
        // Successful response (200) - should not throw
        HttpResponse<String> okResp = new SimpleHttpResponse<>(200, "ok");
        Object okResult = null;
        try {
            okResult = m.invoke(null, "http://dummy", okResp);
        } catch (InvocationTargetException ite) {
            fail("checkResponseOK threw for 200 response: " + ite.getCause());
        }
        // assuming the method is void
        assertNull(okResult);
        // Error response (500) - should throw RuntimeException when invoked
        HttpResponse<String> errResp = new SimpleHttpResponse<>(500, "err");
        try {
            m.invoke(null, "http://dummy", errResp);
            fail("Expected RuntimeException when invoking checkResponseOK with status 500");
        } catch (InvocationTargetException ite) {
            Throwable cause = ite.getCause();
            assertNotNull(cause);
            assertTrue(cause instanceof RuntimeException);
        }
    }
}
