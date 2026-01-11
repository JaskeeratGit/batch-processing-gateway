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

public class LogDao_logApplicationId_5_0_Test_test_logApplicationId_bypassLog_doesNotCallTimerRecord {

    private static void setFinalField(Object target, String fieldName, Object value) throws Exception {
        Field field = LogDao.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        // Remove final modifier so we can set the field
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(target, value);
    }

    private static Object getStaticField(String fieldName) throws Exception {
        Field f = LogDao.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        return f.get(null);
    }

    @Test
    public void test_logApplicationId_bypassLog_doesNotCallTimerRecord() throws Exception {
        // Construct with empty connectionString to set bypassLog = true
        LogDao dao = new LogDao("", "u", "p", "db");
        // Create a TimerMetricContainer that would fail the test if record is called
        TimerMetricContainer fakeTimer = new TimerMetricContainer(new LoggingMeterRegistry()) {

            @Override
            public void record(Runnable runnable, String metricName, Tag... tags) {
                fail("timerMetrics.record should not be called when bypassLog is true");
            }
        };
        // Replace timerMetrics field with our fake
        setFinalField(dao, "timerMetrics", fakeTimer);
        // Call focal method - should simply return without calling timerMetrics.record
        dao.logApplicationId("submission-1", "app-1");
    }


}
