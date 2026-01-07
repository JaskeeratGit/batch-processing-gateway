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
public class HttpUtils_delete_12_0_Test_delete_withNullHeader_doesNotAddHeader_andReturnsBody {


    @Test
    public void delete_withNullHeader_doesNotAddHeader_andReturnsBody() throws Exception {
        try (MockedStatic<HttpRequest> reqStatic = mockStatic(HttpRequest.class);
            MockedStatic<HttpClient> clientStatic = mockStatic(HttpClient.class)) {
            HttpRequest.Builder reqBuilder = mock(HttpRequest.Builder.class);
            HttpRequest requestMock = mock(HttpRequest.class);
            when(reqBuilder.uri(any(URI.class))).thenReturn(reqBuilder);
            // do not stub header, should not be called
            when(reqBuilder.DELETE()).thenReturn(reqBuilder);
            when(reqBuilder.build()).thenReturn(requestMock);
            reqStatic.when(HttpRequest::newBuilder).thenReturn(reqBuilder);
            HttpClient.Builder clientBuilder = mock(HttpClient.Builder.class);
            HttpClient clientMock = mock(HttpClient.class);
            when(clientBuilder.build()).thenReturn(clientMock);
            clientStatic.when(HttpClient::newBuilder).thenReturn(clientBuilder);
            @SuppressWarnings("unchecked")
            HttpResponse<String> responseMock = (HttpResponse<String>) mock(HttpResponse.class);
            when(responseMock.body()).thenReturn("NO-HEADER-BODY");
            when(clientMock.send(eq(requestMock), any(HttpResponse.BodyHandler.class))).thenReturn(responseMock);
            String result = HttpUtils.delete("http://example.com/none", null, null);
            assertEquals("NO-HEADER-BODY", result);
            // verify header was never added
            verify(reqBuilder, never()).header(anyString(), anyString());
            verify(clientMock).send(eq(requestMock), any(HttpResponse.BodyHandler.class));
        }
    }



}
