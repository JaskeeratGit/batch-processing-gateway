package com.apple.spark.rest;

import com.apple.spark.AppConfig;
import com.apple.spark.core.RestStreamingOutput;
import com.apple.spark.util.ConfigUtil;
import com.apple.spark.util.VersionInfo;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
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
import com.apple.spark.util.ExceptionUtils;
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

public class AdminRest_version_3_2_Test_testVersionWritesJsonSuccessfully {

    @Test
    public void testVersionWritesJsonSuccessfully() throws Exception {
        // Prepare a VersionInfo with a known field value
        VersionInfo verInfo = new VersionInfo();
        // set a field via reflection in case VersionInfo has private fields/setters
        try {
            Method m = VersionInfo.class.getMethod("setVersion", String.class);
            m.invoke(verInfo, "1.2.3-test");
        } catch (NoSuchMethodException e) {
            // If no setter, try direct field access
            try {
                java.lang.reflect.Field f = VersionInfo.class.getDeclaredField("version");
                f.setAccessible(true);
                f.set(verInfo, "1.2.3-test");
            } catch (NoSuchFieldException ignored) {
                // If neither exists, the JSON check below will look for the string representation
            }
        }
        // Mock ConfigUtil.readVersion() to return our VersionInfo
        try (MockedStatic<ConfigUtil> cfg = Mockito.mockStatic(ConfigUtil.class)) {
            cfg.when(ConfigUtil::readVersion).thenReturn(verInfo);
            AdminRest admin = new AdminRest(new AppConfig(), new SimpleMeterRegistry());
            Response resp = admin.version();
            assertNotNull(resp);
            assertEquals(200, resp.getStatus());
            Object entity = resp.getEntity();
            assertNotNull(entity);
            assertTrue(entity instanceof RestStreamingOutput);
            RestStreamingOutput output = (RestStreamingOutput) entity;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // Invoke the streaming write - should serialize verInfo into JSON and write with newline
            output.write(baos);
            String result = baos.toString("UTF-8");
            assertNotNull(result);
            // Check the known version string appears in output JSON
            assertTrue(result.contains("1.2.3-test"), "Output JSON should contain the version string we set. Output was: " + result);
            // ensure it ends with a line separator
            assertTrue(result.endsWith(System.lineSeparator()));
        }
    }


}
