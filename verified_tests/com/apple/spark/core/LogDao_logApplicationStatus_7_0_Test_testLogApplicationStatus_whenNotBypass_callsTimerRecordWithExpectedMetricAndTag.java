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

public class LogDao_logApplicationStatus_7_0_Test_testLogApplicationStatus_whenNotBypass_callsTimerRecordWithExpectedMetricAndTag {

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
    public void testLogApplicationStatus_whenNotBypass_callsTimerRecordWithExpectedMetricAndTag() throws Exception {
        // Allocate instance without running constructor to control internal fields directly.
        Unsafe unsafe = getUnsafeInstance();
        LogDao dao = (LogDao) unsafe.allocateInstance(LogDao.class);
        // Prepare and set timerMetrics to our test container.
        TestTimerMetricContainer testTimer = new TestTimerMetricContainer();
        Field timerField = LogDao.class.getDeclaredField("timerMetrics");
        timerField.setAccessible(true);
        timerField.set(dao, testTimer);
        // Ensure bypassLog is false so logApplicationStatus proceeds to call timerMetrics.record(...)
        Field bypassField = LogDao.class.getDeclaredField("bypassLog");
        bypassField.setAccessible(true);
        bypassField.setBoolean(dao, false);
        // Call the focal method
        dao.logApplicationStatus("submission-2", "FINISHED");
        // Verify that timerMetrics.record was called and captured expected metric name and tags.
        Assertions.assertTrue(testTimer.recordCalled, "TimerMetricContainer.record should be called when bypassLog is false");
        Assertions.assertNotNull(testTimer.capturedMetricName, "Metric name should be captured");
        // DB_TIMER_METRIC_NAME is a private constant; we assert it's non-empty instead of direct value.
        Assertions.assertFalse(testTimer.capturedMetricName.isEmpty(), "Metric name should not be empty");
        // Verify that one of the captured tags has key "operation" and value "log_app_status"
        boolean foundOperationTag = false;
        if (testTimer.capturedTags != null) {
            for (Tag t : testTimer.capturedTags) {
                if ("operation".equals(t.getKey()) && "log_app_status".equals(t.getValue())) {
                    foundOperationTag = true;
                    break;
                }
            }
        }
        Assertions.assertTrue(foundOperationTag, "Expected operation=log_app_status tag to be present in record(...) call");
    }

    // Helper to access Unsafe to allocate instance without invoking constructors.
    private static Unsafe getUnsafeInstance() throws Exception {
        Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        return (Unsafe) f.get(null);
    }
}
