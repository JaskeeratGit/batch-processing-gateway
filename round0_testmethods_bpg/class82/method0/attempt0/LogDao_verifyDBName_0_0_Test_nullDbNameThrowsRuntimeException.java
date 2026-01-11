package com.apple.spark.core;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
import java.sql.Timestamp;
import java.util.ArrayList;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogDao_verifyDBName_0_0_Test_nullDbNameThrowsRuntimeException {

    @Test
    void nullDbNameThrowsRuntimeException() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> LogDao.verifyDBName(null));
        assertEquals("DB name should not be null or empty", ex.getMessage());
    }


    @ParameterizedTest
    @ValueSource(strings = { "dbName", "DB123_$", "a", "Z", "0", "$_", "abcDEF123$_", "____", "$$$$", "A1b2C3" })
    void validDbNamesDoNotThrow(String validName) {
        assertDoesNotThrow(() -> LogDao.verifyDBName(validName));
    }

    @ParameterizedTest
    @ValueSource(strings = { "name-with-dash", "name with space", "name!", "name.with.dot", "name%", "name/", "name@", "name#", "name:colon", "name,comma" })
    void invalidDbNamesThrowRuntimeExceptionWithDetailedMessage(String invalidName) {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> LogDao.verifyDBName(invalidName));
        assertEquals("DB name should include only [^0-9a-zA-Z$_]: " + invalidName, ex.getMessage());
    }
}
