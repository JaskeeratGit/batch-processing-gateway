package com.apple.spark.core;

import static org.mockito.ArgumentMatchers.*;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import com.apple.spark.util.TimerMetricContainer;
import com.apple.spark.util.CounterMetricContainer;
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
import java.sql.ResultSet;
import java.util.ArrayList;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogDao_logApplicationFinished_9_0_Test_testBypassLogDoesNothing {

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field f = findField(target.getClass(), fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static Field findField(Class<?> clazz, String name) throws NoSuchFieldException {
        Class<?> c = clazz;
        while (c != null) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                c = c.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    @Test
    public void testBypassLogDoesNothing() throws Exception {
        // create instance with empty connection string -> bypassLog == true
        LogDao dao = new LogDao("", "u", "p", "db", new LoggingMeterRegistry());
        // replace the timerMetrics with a mock to assert it is not invoked
        TimerMetricContainer mockTimer = mock(TimerMetricContainer.class);
        setField(dao, "timerMetrics", mockTimer);
        // Call method; since bypassLog is true, timerMetrics.record should not be called
        dao.logApplicationFinished("sub-1", "FINISHED", new Timestamp(System.currentTimeMillis()), 1.0, 2.0);
        verifyNoInteractions(mockTimer);
    }


}
