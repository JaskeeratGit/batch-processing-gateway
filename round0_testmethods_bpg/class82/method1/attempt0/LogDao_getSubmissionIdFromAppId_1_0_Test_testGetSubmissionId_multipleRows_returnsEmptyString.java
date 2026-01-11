package com.apple.spark.core;

import static org.mockito.ArgumentMatchers.anyInt;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.function.Supplier;
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
import java.sql.Timestamp;
import java.util.ArrayList;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JUnit5 tests for LogDao#getSubmissionIdFromAppId(String)
 *
 * Notes:
 * - Uses reflection to invoke the private implementation method getSubmissionIdFromAppIdImpl.
 * - Uses Mockito to mock JDBC objects and a mocked DBConnection.
 * - Overrides LogDao's timerMetrics with a test TimerMetricContainer that simply invokes the supplier
 *   and returns the result (so public getSubmissionIdFromAppId delegates to the impl).
 */
public class LogDao_getSubmissionIdFromAppId_1_0_Test_testGetSubmissionId_multipleRows_returnsEmptyString {

    private LogDao dao;

    private com.apple.spark.core.DBConnection mockDbConnection;

    private TimerMetricContainer testTimerMetrics;

    private CounterMetricContainer testFailureMetrics;

    @BeforeEach
    public void setUp() throws Exception {
        // Create LogDao instance without invoking its constructor to avoid DB initialization side effects.
        dao = (LogDao) allocateInstance(LogDao.class);
        // Create mocks
        mockDbConnection = mock(com.apple.spark.core.DBConnection.class);
        // Provide simple metric containers (CounterMetricContainer provided in project; we pass LoggingMeterRegistry)
        testFailureMetrics = new CounterMetricContainer(new LoggingMeterRegistry());
        testTimerMetrics = new TestTimerMetricContainer(new LoggingMeterRegistry());
        // Inject mocks / test metrics into the private fields of LogDao
        setPrivateField(dao, "dbConnection", mockDbConnection);
        setPrivateField(dao, "failureMetrics", testFailureMetrics);
        setPrivateField(dao, "timerMetrics", testTimerMetrics);
        // also set dbName to avoid potential NPE in SQL building (the code uses dbName)
        setPrivateField(dao, "dbName", "testdb");
        // ensure bypassLog false
        setPrivateField(dao, "bypassLog", false);
    }



    @Test
    public void testGetSubmissionId_multipleRows_returnsEmptyString() throws Exception {
        // Arrange
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);
        when(mockDbConnection.getConnection()).thenReturn(mockConn);
        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
        // simulate two rows -> should detect multiple records and return empty string
        when(mockRs.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(mockRs.getObject("submission_id")).thenReturn("s1").thenReturn("s2");
        // Act
        String result = dao.getSubmissionIdFromAppId("app-3");
        // Assert
        assertEquals("", result);
    }


    // -------------------------
    // Helper utilities
    // -------------------------
    /**
     * Test TimerMetricContainer that invokes the supplier and returns its value. This mirrors the
     * expected TimerMetricContainer.record(Supplier<T>...) behavior that the real LogDao relies on.
     */
    private static class TestTimerMetricContainer extends TimerMetricContainer {

        public TestTimerMetricContainer(io.micrometer.core.instrument.MeterRegistry mr) {
            super(mr);
        }

        @SuppressWarnings("unused")
        public <T> T record(Supplier<T> supplier, String metricName, Tag... tags) {
            return supplier.get();
        }
    }

    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field f = null;
        Class<?> cls = target.getClass();
        while (cls != null) {
            try {
                f = cls.getDeclaredField(fieldName);
                break;
            } catch (NoSuchFieldException e) {
                cls = cls.getSuperclass();
            }
        }
        if (f == null) {
            throw new NoSuchFieldException(fieldName);
        }
        f.setAccessible(true);
        f.set(target, value);
    }

    /**
     * Allocates an instance of the given class without invoking any constructor (uses Unsafe).
     */
    private static Object allocateInstance(Class<?> cls) throws Exception {
        // use sun.misc.Unsafe to allocate instance without constructor side-effects
        Field theUnsafe = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        sun.misc.Unsafe unsafe = (sun.misc.Unsafe) theUnsafe.get(null);
        return unsafe.allocateInstance(cls);
    }
}
