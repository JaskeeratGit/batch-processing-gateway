package com.apple.spark.core;

import java.lang.reflect.Method;
import java.sql.Timestamp;
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
import com.apple.spark.util.TimerMetricContainer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogDao_updateJobInfo_12_0_Test {

    @Test
    public void testUpdateJobInfo_BypassLog_returnsTrue() {
        // When connectionString is empty, constructor sets bypassLog = true,
        // updateJobInfo should return true immediately.
        LogDao dao = new LogDao("", "user", "pass", "mydb");
        boolean result = dao.updateJobInfo("submission-1", new Timestamp(System.currentTimeMillis()), 2, 1024, 1, "dagA", "taskA", "appA");
        assertTrue(result, "updateJobInfo should return true when bypassLog is enabled");
    }

    @Test
    public void testUpdateJobInfoImpl_whenDbConnectionNull_returnsFalse() throws Exception {
        // Create LogDao with bypassLog true (empty connection string) so constructor runs.
        LogDao dao = new LogDao("", "user", "pass", "mydb");
        // Use reflection to invoke the private updateJobInfoImpl method directly.
        Method m = LogDao.class.getDeclaredMethod("updateJobInfoImpl", String.class, Timestamp.class, int.class, int.class, int.class, String.class, String.class, String.class);
        m.setAccessible(true);
        // Because dbConnection is null in this instance, calling updateJobInfoImpl should trigger
        // an exception inside the method which is caught and causes the method to return false.
        Object returned = m.invoke(dao, "submission-2", new Timestamp(System.currentTimeMillis()), 4, 2048, 2, "dagB", "taskB", "appB");
        assertEquals(Boolean.FALSE, returned, "updateJobInfoImpl should return false when DB interaction fails");
    }
}
