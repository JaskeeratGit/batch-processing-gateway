package com.apple.spark.util;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * JUnit 5 tests for HttpUtils.delete(String, String, String, Class)
 *
 * These tests start a local HttpServer to exercise the HTTP DELETE behavior and also use reflection
 * to invoke the private parseJson method on HttpUtils to increase coverage of private parsing logic.
 */
public class HttpUtils_delete_11_0_Test_testParseJsonPrivateMethodViaReflection {

    private static HttpServer server;

    private static int port;

    // Use a fixed header name for the tests
    private static final String TEST_HEADER = "X-Test-Header";

    @BeforeAll
    public static void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        port = server.getAddress().getPort();
        // Context that responds to DELETE requests and returns a JSON body indicating whether the header was present and its value
        server.createContext("/test-delete", new HttpHandler() {

            @Override
            public void handle(HttpExchange exchange) throws IOException {
                try {
                    if (!"DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
                        exchange.sendResponseHeaders(405, -1);
                        return;
                    }
                    String headerValue = null;
                    if (exchange.getRequestHeaders().containsKey(TEST_HEADER)) {
                        var vals = exchange.getRequestHeaders().get(TEST_HEADER);
                        if (vals != null && !vals.isEmpty()) {
                            headerValue = vals.get(0);
                        }
                    }
                    // Build JSON response
                    String responseJson = String.format("{\"receivedHeader\":%s}", headerValue == null ? "null" : ("\"" + escapeJson(headerValue) + "\""));
                    byte[] bytes = responseJson.getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
                    exchange.sendResponseHeaders(200, bytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(bytes);
                    }
                } finally {
                    exchange.close();
                }
            }
        });
        server.start();
    }

    @AfterAll
    public static void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    private static String buildUrl(String path) {
        return String.format("http://127.0.0.1:%d%s", port, path);
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // DTO used for JSON mapping in tests
    public static class ResponseDto {

        // Keep fields public so Jackson can populate them
        public String receivedHeader;

        public ResponseDto() {
        }
    }





    @Test
    public void testParseJsonPrivateMethodViaReflection() throws Exception {
        // Access private static parseJson(String, Class) via reflection
        Method parseMethod = HttpUtils.class.getDeclaredMethod("parseJson", String.class, Class.class);
        parseMethod.setAccessible(true);
        String json = "{\"receivedHeader\":\"reflected-value\"}";
        Object result = parseMethod.invoke(null, json, ResponseDto.class);
        assertNotNull(result);
        assertTrue(result instanceof ResponseDto);
        ResponseDto dto = (ResponseDto) result;
        assertEquals("reflected-value", dto.receivedHeader);
    }
}
