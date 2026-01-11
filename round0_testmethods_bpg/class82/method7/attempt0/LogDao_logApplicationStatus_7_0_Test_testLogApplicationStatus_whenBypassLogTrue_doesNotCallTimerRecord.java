package com.apple.spark.core;

import com.apple.spark.util.TimerMetricContainer;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import java.lang.reflect.Field;
import sun.misc.Unsafe;
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

public class LogDao_logApplicationStatus_7_0_Test_testLogApplicationStatus_whenBypassLogTrue_doesNotCallTimerRecord {

    // A small test helper that captures calls to record(...) without executing the Runnable.
    private static class TestTimerMetricContainer extends TimerMetricContainer {

        volatile boolean recordCalled = false;

        volatile String capturedMetricName = null;

        volatile Tag[] capturedTags = null;

        volatile Runnable capturedRunnable = null;

        TestTimerMetricContainer() {
            super(new LoggingMeterRegistry());
        }

        @Override
        public void record(Runnable runnable, String metricName, Tag... tags) {
            this.recordCalled = true;
            this.capturedRunnable = runnable;
            this.capturedMetricName = metricName;
            this.capturedTags = tags;
            // Intentionally do not run the runnable to avoid touching DB-related code.
        }
    }

    @Test
    public void testLogApplicationStatus_whenBypassLogTrue_doesNotCallTimerRecord() throws Exception {
        // Create LogDao via public constructor with empty connectionString so bypassLog is set to true.
        LogDao dao = new LogDao("", "", "", "");
        // Replace timerMetrics with our test container to observe any calls.
        TestTimerMetricContainer testTimer = new TestTimerMetricContainer();
        Field timerField = LogDao.class.getDeclaredField("timerMetrics");
        timerField.setAccessible(true);
        timerField.set(dao, testTimer);
        // Call the focal method
        dao.logApplicationStatus("submission-1", "RUNNING");
        // Because bypassLog should be true for this instance, record should NOT be called.
        Assertions.assertFalse(testTimer.recordCalled, "TimerMetricContainer.record should not be called when bypassLog is true");
    }


    // Helper to access Unsafe to allocate instance without invoking constructors.
    private static Unsafe getUnsafeInstance() throws Exception {
        Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        return (Unsafe) f.get(null);
    }
}
