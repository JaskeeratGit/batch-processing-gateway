package com.apple.spark.core;

import static org.mockito.ArgumentMatchers.*;
import com.apple.spark.util.CounterMetricContainer;
import com.apple.spark.util.TimerMetricContainer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
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
import java.sql.Timestamp;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogDao_getKeyList_20_0_Test_testGetKeyList_onPrepareStatementThrows_incrementsFailureMetricAndReturnsEmpty {

    private static final String DB_NAME = "testdb";

    private LogDao logDao;

    @BeforeEach
    public void setUp() {
        // construct LogDao with empty connection string so it won't attempt real DB ops during ctor
        logDao = new LogDao("", "", "", DB_NAME, new LoggingMeterRegistry());
    }


    @Test
    public void testGetKeyList_onPrepareStatementThrows_incrementsFailureMetricAndReturnsEmpty() throws Exception {
        // mocks
        DBConnection mockDbConnection = Mockito.mock(DBConnection.class);
        Connection mockConnection = Mockito.mock(Connection.class);
        CounterMetricContainer mockFailureMetrics = Mockito.mock(CounterMetricContainer.class);
        // make prepareStatement throw
        when(mockDbConnection.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("boom"));
        // inject mocks
        setPrivateField(logDao, "dbConnection", mockDbConnection);
        setPrivateField(logDao, "failureMetrics", mockFailureMetrics);
        // call focal method with execId that ends with "driver" to exercise driver branch
        ArrayList<String> result = logDao.getKeyList("mypod", "driver");
        // should return empty list on exception
        assertNotNull(result);
        assertTrue(result.isEmpty());
        // verify that failureMetrics.increment was called once with expected kinds of args
        verify(mockFailureMetrics, times(1)).increment(anyString(), any(Tag.class), any(Tag.class));
    }

    // helper to set private fields via reflection
    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = LogDao.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
