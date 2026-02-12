package com.apple.spark.core;

import com.apple.spark.util.TimerMetricContainer;
import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import io.micrometer.core.instrument.Tag;
import java.lang.reflect.Field;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LogDao_updateExecutorInfo_18_0_Test_testUpdateExecutorInfo_timerReturnsTrue_returnsTrue {

    /**
     * Helper to set private fields via reflection.
     */
    private static void setField(Object target, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }


    @Test
    public void testUpdateExecutorInfo_timerReturnsTrue_returnsTrue() throws Exception {
        // Start with bypass=true instance to avoid DB initialization in constructor
        LogDao dao = new LogDao("", "", "", "");
        // Set bypassLog to false so the method will go through timerMetrics.record
        setField(dao, "bypassLog", false);
        // Inject a TimerMetricContainer that returns true from record without invoking the supplier
        TimerMetricContainer timerStub = new TimerMetricContainer(new LoggingMeterRegistry()) {

            @Override
            @SuppressWarnings("unchecked")
            public <T> T record(Supplier<T> supplier, String metricName, Tag... tags) {
                // Do not call supplier, simulate a successful metric-wrapped operation
                return (T) Boolean.TRUE;
            }
        };
        setField(dao, "timerMetrics", timerStub);
        boolean result = dao.updateExecutorInfo("submission-2", 4, 2048);
        assertTrue(result, "When timerMetrics.record returns true, updateExecutorInfo should return true");
    }

}
