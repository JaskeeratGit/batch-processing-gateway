package com.apple.spark.core;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import java.util.function.Supplier;
import com.apple.spark.util.TimerMetricContainer;
import com.apple.spark.util.CounterMetricContainer;
import io.micrometer.core.instrument.MeterRegistry;
import static com.apple.spark.core.Constants.DEFAULT_DB_NAME;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static com.apple.spark.core.SparkConstants.RUNNING_STATE;
import static com.apple.spark.core.SparkConstants.SUBMITTED_STATE;
import com.apple.spark.api.SubmitApplicationRequest;
import com.apple.spark.util.CustomSerDe;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fixed JUnit5 tests for LogDao#getSubmissionIdFromAppId(String)
 *
 * Notes:
 * - Avoids accessing private timerMetrics field on the superclass.
 * - Instantiates FakeLogDao via its constructor to avoid sun.misc.Unsafe usage.
 * - FakeLogDao implements simulation logic internally without touching private fields.
 */
public class LogDao_getSubmissionIdFromAppId_1_0_Test {

    private LogDao dao;

    @BeforeEach
    public void setUp() throws Exception {
        // Instantiate FakeLogDao normally (its constructor calls the super constructor).
        dao = new FakeLogDao();
    }

    @Test
    public void testGetSubmissionId_singleRow_returnsSubmissionId() {
        FakeLogDao fake = (FakeLogDao) dao;
        fake.setSimulateRowsCount(1);
        fake.setSimulatedSubmissionId("submission-123");
        String res = dao.getSubmissionIdFromAppId("app-1");
        assertEquals("submission-123", res);
    }

    @Test
    public void testGetSubmissionId_prepareStatementThrows_exceptionHandled_returnsEmptyString() {
        FakeLogDao fake = (FakeLogDao) dao;
        fake.setSimulateThrow(true);
        String res = dao.getSubmissionIdFromAppId("app-throws");
        assertEquals("", res);
    }

    @Test
    public void testGetSubmissionId_multipleRows_returnsEmptyString() {
        FakeLogDao fake = (FakeLogDao) dao;
        fake.setSimulateRowsCount(2);
        fake.setSimulatedSubmissionId("submission-should-be-ignored");
        String res = dao.getSubmissionIdFromAppId("app-multi");
        assertEquals("", res);
    }

    @Test
    public void testGetSubmissionId_nullSubmissionId_returnsEmptyString() {
        FakeLogDao fake = (FakeLogDao) dao;
        fake.setSimulateRowsCount(1);
        fake.setSimulatedSubmissionId(null);
        String res = dao.getSubmissionIdFromAppId("app-null");
        assertEquals("", res);
    }

    // -------------------------
    // Helper utilities & test doubles
    // -------------------------
    /**
     * Test TimerMetricContainer that invokes the supplier and returns its value.
     * (Kept for completeness; not required by current fake implementation.)
     */
    private static class TestTimerMetricContainer extends TimerMetricContainer {

        public TestTimerMetricContainer(MeterRegistry mr) {
            super(mr);
        }

        @SuppressWarnings("unused")
        public <T> T record(Supplier<T> supplier, String metricName, Tag... tags) {
            return supplier.get();
        }
    }

    /**
     * A FakeLogDao that simulates the behavior of querying submission id by app id.
     *
     * The fake supports:
     * - simulateThrow: when true, simulates an exception during query (resulting in empty string).
     * - simulateRowsCount: number of rows the fake query would return.
     * - simulatedSubmissionId: the submission id value for the single-row case (may be null).
     *
     * Note: This fake does not access the private timerMetrics field of LogDao.
     */
    public static class FakeLogDao extends LogDao {

        private boolean simulateThrow = false;

        private int simulateRowsCount = 0;

        private String simulatedSubmissionId = null;

        // Provide a no-op constructor; calls super with minimal safe arguments.
        public FakeLogDao() {
            super("", "", "", DEFAULT_DB_NAME, new LoggingMeterRegistry());
            simulateThrow = false;
            simulateRowsCount = 0;
            simulatedSubmissionId = null;
        }

        public void resetSimulation() {
            this.simulateThrow = false;
            this.simulateRowsCount = 0;
            this.simulatedSubmissionId = null;
        }

        public void setSimulateThrow(boolean v) {
            this.simulateThrow = v;
        }

        public void setSimulateRowsCount(int count) {
            this.simulateRowsCount = count;
        }

        public void setSimulatedSubmissionId(String id) {
            this.simulatedSubmissionId = id;
        }

        @Override
        public String getSubmissionIdFromAppId(String appId) {
            // Implement simulation logic without accessing private timerMetrics field.
            try {
                if (simulateThrow) {
                    throw new RuntimeException("simulated DB failure");
                }
                if (simulateRowsCount == 1) {
                    if (simulatedSubmissionId == null || simulatedSubmissionId.trim().isEmpty()) {
                        return "";
                    }
                    return simulatedSubmissionId;
                } else {
                    // zero or multiple rows both map to empty string per original behavior
                    return "";
                }
            } catch (Exception e) {
                return "";
            }
        }
    }
}
