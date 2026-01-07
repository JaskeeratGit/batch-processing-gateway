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

public class HttpUtils_getHttpResponse_15_0_Test_testGetHttpResponse_connectionFailureWrapped {

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
    public void testGetHttpResponse_connectionFailureWrapped() {
        // Use a port that is very likely closed to cause a connection failure (send will throw)
        String url = "http://127.0.0.1:1/some";
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            HttpUtils.getHttpResponse(url, null, null);
        });
        assertTrue(ex.getMessage().contains(url));
        assertNotNull(ex.getCause());
    }

}
