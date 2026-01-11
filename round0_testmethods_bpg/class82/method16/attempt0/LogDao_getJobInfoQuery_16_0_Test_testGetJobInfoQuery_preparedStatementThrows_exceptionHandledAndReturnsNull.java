package com.apple.spark.core;

import static org.mockito.ArgumentMatchers.anyString;
import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
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
import java.sql.Timestamp;
import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogDao_getJobInfoQuery_16_0_Test_testGetJobInfoQuery_preparedStatementThrows_exceptionHandledAndReturnsNull {

    private DBConnection mockDbConnection;

    private Connection mockConnection;

    private PreparedStatement mockPreparedStatement;

    private CachedRowSet emptyCachedRowSet;

    private LogDao dao;

    @BeforeEach
    public void setUp() throws Exception {
        mockDbConnection = Mockito.mock(DBConnection.class);
        mockConnection = Mockito.mock(Connection.class);
        mockPreparedStatement = Mockito.mock(PreparedStatement.class);
        emptyCachedRowSet = RowSetProvider.newFactory().createCachedRowSet();
        when(mockDbConnection.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(emptyCachedRowSet);
        // Create LogDao with empty connectionString so dbConnection is null initially and we can inject mock.
        dao = new LogDao("", "user", "password", "testdb", new LoggingMeterRegistry());
        // Inject mockDbConnection into private final field dbConnection
        Field dbConnField = LogDao.class.getDeclaredField("dbConnection");
        dbConnField.setAccessible(true);
        dbConnField.set(dao, mockDbConnection);
    }



    @Test
    public void testGetJobInfoQuery_preparedStatementThrows_exceptionHandledAndReturnsNull() throws Throwable {
        // Arrange: make prepareStatement throw an exception to exercise catch block
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("boom"));
        String status = "SOME_STATUS";
        String user = "all";
        int queryLimit = 3;
        int numDaysToShow = 1;
        // Act
        Method impl = LogDao.class.getDeclaredMethod("getJobInfoQueryImpl", String.class, String.class, int.class, int.class);
        impl.setAccessible(true);
        ResultSet result = (ResultSet) impl.invoke(dao, status, user, queryLimit, numDaysToShow);
        // Assert: when an exception occurs, implementation returns null (crs stays null)
        assertNull(result, "Expected null ResultSet when SQL execution fails");
        // verify prepareStatement was attempted
        verify(mockConnection, atLeastOnce()).prepareStatement(anyString());
    }
}
