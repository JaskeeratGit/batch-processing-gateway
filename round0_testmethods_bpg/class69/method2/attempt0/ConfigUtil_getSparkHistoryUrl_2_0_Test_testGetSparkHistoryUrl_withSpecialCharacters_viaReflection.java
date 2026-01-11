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

public class ConfigUtil_getSparkHistoryUrl_2_0_Test_testGetSparkHistoryUrl_withSpecialCharacters_viaReflection {




    @Test
    void testGetSparkHistoryUrl_withSpecialCharacters_viaReflection() throws Exception {
        Class<?> clazz = Class.forName("com.apple.spark.util.ConfigUtil");
        Method method = clazz.getDeclaredMethod("getSparkHistoryUrl", String.class, String.class);
        method.setAccessible(true);
        String sparkHistoryDns = "host:9090";
        String appId = "user/app#1";
        Object result = method.invoke(null, sparkHistoryDns, appId);
        String str = (String) result;
        assertTrue(str.startsWith("https://"));
        assertTrue(str.contains("/history/"));
        assertEquals("https://host:9090/history/user/app#1", str);
    }
}
