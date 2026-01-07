package com.apple.spark.core;

import com.apple.spark.util.TimerMetricContainer;
import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import io.micrometer.core.instrument.Tag;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Supplier;
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
import com.apple.spark.util.CounterMetricContainer;
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

public class LogDao_updateExecutorInfo_18_0_Test {

    /**
     * Helper to set private fields via reflection.
     */
    private static void setField(Object target, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    @Test
    public void testBypassLogTrue_returnsTrue() {
        // Construct LogDao with empty connection string so constructor sets bypassLog = true
        LogDao dao = new LogDao("", "", "", "");
        boolean result = dao.updateExecutorInfo("submission-1", 2, 1024);
        assertTrue(result, "When bypassLog is true updateExecutorInfo should return true");
    }

    @Test
    public void testUpdateExecutorInfo_timerReturnsTrue_returnsTrue() throws Exception {
        // Start with bypass=true instance to avoid DB initialization in constructor
        LogDao dao = new LogDao("", "", "", "");
        // Set bypassLog to false so the method will go through timerMetrics.record
        setField(dao, "bypassLog", false);
        // Inject a TimerMetricContainer that returns true from record without invoking the supplier
        TimerMetricContainer timerStub = new TimerMetricContainer(new LoggingMeterRegistry()) {

            @SuppressWarnings({ "unchecked", "rawtypes" })
            public <T> T record(Supplier<T> supplier, String metricName, Tag... tags) {
                // Do not call supplier, simulate a successful metric-wrapped operation
                return (T) Boolean.TRUE;
            }
        };
        setField(dao, "timerMetrics", timerStub);
        boolean result = dao.updateExecutorInfo("submission-2", 4, 2048);
        assertTrue(result, "When timerMetrics.record returns true, updateExecutorInfo should return true");
    }

    @Test
    public void testUpdateExecutorInfo_implThrows_returnsFalse_and_privateMethod_invocation() throws Exception {
        // Create instance with bypass true to avoid DB ops in constructor
        LogDao dao = new LogDao("", "", "", "");
        // Set bypassLog false so updateExecutorInfo uses timerMetrics.record
        setField(dao, "bypassLog", false);
        // Inject a TimerMetricContainer that invokes the supplier (so it will call the private impl)
        TimerMetricContainer timerInvoker = new TimerMetricContainer(new LoggingMeterRegistry()) {

            public <T> T record(Supplier<T> supplier, String metricName, Tag... tags) {
                // Call through to the supplier which will execute updateExecutorInfoImpl
                return supplier.get();
            }
        };
        setField(dao, "timerMetrics", timerInvoker);
        // At this point dbConnection is null (because of how we constructed dao), so private impl will
        // throw/catch and return false. The public updateExecutorInfo should therefore return false.
        boolean publicResult = dao.updateExecutorInfo("submission-3", 1, 512);
        assertFalse(publicResult, "When updateExecutorInfoImpl fails (dbConnection null), updateExecutorInfo should return false");
        // Additionally invoke the private updateExecutorInfoImpl directly via reflection to ensure the
        // private method returns false when dbConnection is null (covers private method branch).
        Method m = LogDao.class.getDeclaredMethod("updateExecutorInfoImpl", String.class, int.class, int.class);
        m.setAccessible(true);
        Object privateResult = m.invoke(dao, "submission-3", 1, 512);
        assertTrue(privateResult instanceof Boolean, "Private method should return a Boolean");
        assertFalse(((Boolean) privateResult), "Private updateExecutorInfoImpl should return false when failing");
    }
}
