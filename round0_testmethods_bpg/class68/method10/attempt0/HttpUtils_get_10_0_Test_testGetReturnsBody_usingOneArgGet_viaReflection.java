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

public class HttpUtils_get_10_0_Test_testGetReturnsBody_usingOneArgGet_viaReflection {

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


}
