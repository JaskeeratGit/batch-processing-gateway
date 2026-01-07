package com.apple.spark.rest;

import com.apple.spark.AppConfig;
import com.apple.spark.core.RestStreamingOutput;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import javax.ws.rs.core.Response;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static com.apple.spark.core.Constants.ADMIN_API;
import com.apple.spark.core.Constants;
import com.apple.spark.core.KubernetesHelper;
import com.apple.spark.core.RestSubmissionsStreamingOutput;
import com.apple.spark.operator.SparkApplicationResourceList;
import com.apple.spark.security.User;
import com.apple.spark.util.ConfigUtil;
import com.apple.spark.util.ExceptionUtils;
import com.apple.spark.util.VersionInfo;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.auth.Auth;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import javax.annotation.security.PermitAll;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminRest_version_3_1_Test_version_shouldHandleOutputStreamWriteFailuresGracefully {


    @Test
    public void version_shouldHandleOutputStreamWriteFailuresGracefully() throws Exception {
        AdminRest adminRest = new AdminRest(new AppConfig(), new SimpleMeterRegistry());
        Response resp = adminRest.version();
        assertEquals(200, resp.getStatus());
        Object entity = resp.getEntity();
        assertTrue(entity instanceof RestStreamingOutput);
        RestStreamingOutput streamingOutput = (RestStreamingOutput) entity;
        // Create an OutputStream that throws on write to trigger the catch block in write()
        OutputStream failingOutput = new OutputStream() {

            @Override
            public void write(int b) throws IOException {
                throw new IOException("simulated write failure");
            }
        };
        // The implementation catches Throwable internally and should not propagate an exception.
        // If an exception is thrown here the test will fail.
        streamingOutput.write(failingOutput);
    }
}
