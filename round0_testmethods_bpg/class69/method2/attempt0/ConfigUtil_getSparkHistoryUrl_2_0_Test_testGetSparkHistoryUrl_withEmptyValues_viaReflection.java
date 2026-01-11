package com.apple.spark.util;

import java.lang.reflect.Method;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static com.apple.spark.AppConfig.SparkCluster;
import com.apple.spark.AppConfig;
import com.apple.spark.core.DBConnection;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigUtil_getSparkHistoryUrl_2_0_Test_testGetSparkHistoryUrl_withEmptyValues_viaReflection {


    @Test
    void testGetSparkHistoryUrl_withEmptyValues_viaReflection() throws Exception {
        Class<?> clazz = Class.forName("com.apple.spark.util.ConfigUtil");
        Method method = clazz.getDeclaredMethod("getSparkHistoryUrl", String.class, String.class);
        method.setAccessible(true);
        String sparkHistoryDns = "";
        String appId = "";
        Object result = method.invoke(null, sparkHistoryDns, appId);
        // Expect the DNS and appId to be placed into the format as-is (empty strings)
        assertEquals("https:///history/", result);
    }


}
