package com.apple.spark.util;

import static org.mockito.ArgumentMatchers.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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

/**
 * JUnit 5 tests for HttpUtils.delete(...)
 *
 * Note: These tests use Mockito's static mocking (mockStatic). Ensure Mockito (with inline mock maker)
 * is available on the test classpath (e.g., mockito-inline).
 */
public class HttpUtils_delete_12_0_Test {

    @Test
    public void delete_invalidUrl_throwsRuntimeException_wrappingURISyntaxException() {
        // No need to mock HttpRequest / HttpClient because new URI(url) will throw first.
        // invalid IPv6 host format for URI
        String badUrl = "http://[invalid-url]";
        RuntimeException ex = assertThrows(RuntimeException.class, () -> HttpUtils.delete(badUrl, null, null));
        assertTrue(ex.getMessage().contains("Failed to delete"));
        assertTrue(ex.getMessage().contains(badUrl));
        assertNotNull(ex.getCause());
        assertTrue(ex.getCause() instanceof URISyntaxException);
    }
}
