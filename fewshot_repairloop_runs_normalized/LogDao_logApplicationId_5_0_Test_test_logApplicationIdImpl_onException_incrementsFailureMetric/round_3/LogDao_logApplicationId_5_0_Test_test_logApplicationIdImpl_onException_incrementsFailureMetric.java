package com.apple.spark.core;

import static org.mockito.ArgumentMatchers.*;
import com.apple.spark.util.TimerMetricContainer;
import com.apple.spark.util.CounterMetricContainer;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicReference;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static com.apple.spark.core.Constants.DEFAULT_DB_NAME;
import static com.apple.spark.core.SparkConstants.RUNNING_STATE;
import static com.apple.spark.core.SparkConstants.SUBMITTED_STATE;
import com.apple.spark.api.SubmitApplicationRequest;
import com.apple.spark.util.CustomSerDe;
import io.micrometer.core.instrument.MeterRegistry;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogDao_logApplicationId_5_0_Test_test_logApplicationIdImpl_onException_incrementsFailureMetric {

    private static void setFinalField(Object target, String fieldName, Object value) throws Exception {
        Field field = LogDao.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        // Attempt to set the field value via reflection. Avoid manipulating Field.modifiers for compatibility.
        field.set(target, value);
    }

    private static Object getStaticField(String fieldName) throws Exception {
        Field f = LogDao.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        return f.get(null);
    }



    @Test
    public void test_logApplicationIdImpl_onException_incrementsFailureMetric() throws Exception {
        // Create LogDao with empty connection string so dbConnection==null and bypassLog==true
        LogDao dao = new LogDao("", "u", "p", "db");
        // Replace failureMetrics with a Mockito mock to verify increment invocation
        CounterMetricContainer mockFailureMetrics = mock(CounterMetricContainer.class);
        setFinalField(dao, "failureMetrics", mockFailureMetrics);
        // Ensure bypassLog is false so private impl could be invoked directly if needed
        Field bypassField = LogDao.class.getDeclaredField("bypassLog");
        bypassField.setAccessible(true);
        bypassField.setBoolean(dao, false);
        // Invoke private method logApplicationIdImpl via reflection
        Method impl = LogDao.class.getDeclaredMethod("logApplicationIdImpl", String.class, String.class);
        impl.setAccessible(true);
        // Call with dbConnection == null to force a NullPointerException inside impl,
        // which should be caught and result in failureMetrics.increment(...)
        impl.invoke(dao, "s-ex", "a-ex");
        // Verify failureMetrics.increment was called with expected metric name and tags
        String expectedFailureMetric = (String) getStaticField("DB_FAILURE_METRIC_NAME");
        String expectedOp = (String) getStaticField("LOG_APP_ID_OPERATION");
        // Capture the varargs tags and assert they were provided
        ArgumentCaptor<Object> tagsCaptor = ArgumentCaptor.forClass(Object.class);
        verify(mockFailureMetrics, atLeastOnce()).increment(eq(expectedFailureMetric), tagsCaptor.capture());
        Object capturedObj = tagsCaptor.getValue();
        assertNotNull(capturedObj, "Expected tags to be supplied to failureMetrics.increment");
        Tag[] captured;
        if (capturedObj instanceof Tag[]) {
            captured = (Tag[]) capturedObj;
        } else if (capturedObj instanceof Tag) {
            captured = new Tag[] { (Tag) capturedObj };
        } else if (capturedObj instanceof Object[]) {
            Object[] arr = (Object[]) capturedObj;
            captured = new Tag[arr.length];
            for (int i = 0; i < arr.length; i++) {
                captured[i] = (Tag) arr[i];
            }
        } else {
            fail("Unexpected type for captured tags: " + capturedObj.getClass());
            return;
        }
        assertTrue(captured.length >= 1, "Expected at least one tag supplied to failureMetrics.increment");
    }
}
