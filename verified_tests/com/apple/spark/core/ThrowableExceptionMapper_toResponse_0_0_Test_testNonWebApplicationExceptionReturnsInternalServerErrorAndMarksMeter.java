package com.apple.spark.core;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import io.dropwizard.jersey.errors.ErrorMessage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.apple.spark.util.ExceptionUtils;
import javax.ws.rs.ext.ExceptionMapper;
import org.glassfish.jersey.server.ParamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThrowableExceptionMapper_toResponse_0_0_Test_testNonWebApplicationExceptionReturnsInternalServerErrorAndMarksMeter {

    @Test
    public void testNonWebApplicationExceptionReturnsInternalServerErrorAndMarksMeter() throws Exception {
        MetricRegistry metrics = new MetricRegistry();
        // create mapper
        ThrowableExceptionMapper mapper = new ThrowableExceptionMapper(metrics);
        // access private 'exceptions' field via reflection
        Field exceptionsField = ThrowableExceptionMapper.class.getDeclaredField("exceptions");
        exceptionsField.setAccessible(true);
        Meter exceptionsMeter = (Meter) exceptionsField.get(mapper);
        long before = exceptionsMeter.getCount();
        // prepare throwable
        RuntimeException ex = new RuntimeException("boom");
        // invoke toResponse via reflection (satisfies reflective invocation requirement)
        Method toResponse = ThrowableExceptionMapper.class.getDeclaredMethod("toResponse", Throwable.class);
        toResponse.setAccessible(true);
        Response response = (Response) toResponse.invoke(mapper, ex);
        // verify response
        assertNotNull(response);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());
        // entity should be ErrorMessage with correct code and message
        Object entity = response.getEntity();
        assertNotNull(entity);
        assertTrue(entity instanceof ErrorMessage);
        ErrorMessage em = (ErrorMessage) entity;
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), em.getCode());
        String expectedMessage = com.apple.spark.util.ExceptionUtils.getExceptionNameAndMessage(ex);
        assertEquals(expectedMessage, em.getMessage());
        // meter should have been marked once
        assertEquals(before + 1, exceptionsMeter.getCount());
    }

}
