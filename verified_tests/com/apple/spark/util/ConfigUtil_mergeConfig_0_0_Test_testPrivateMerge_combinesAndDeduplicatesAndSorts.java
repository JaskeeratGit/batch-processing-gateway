package com.apple.spark.util;

import com.apple.spark.AppConfig;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static com.apple.spark.AppConfig.SparkCluster;
import com.apple.spark.core.DBConnection;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.InputStream;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigUtil_mergeConfig_0_0_Test_testPrivateMerge_combinesAndDeduplicatesAndSorts {

    /**
     * Helper to instantiate AppConfig.SparkCluster via reflection and set its id (via setter or field).
     */
    private Object newSparkCluster(String id) throws Exception {
        Class<?> scClass = Class.forName("com.apple.spark.AppConfig$SparkCluster");
        Constructor<?> ctor = scClass.getDeclaredConstructor();
        ctor.setAccessible(true);
        Object sc = ctor.newInstance();
        // Try setter setId(String)
        try {
            Method setId = scClass.getMethod("setId", String.class);
            setId.invoke(sc, id);
            return sc;
        } catch (NoSuchMethodException ignored) {
            // try field
        }
        // Try field 'id'
        try {
            Field idField = scClass.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(sc, id);
            return sc;
        } catch (NoSuchFieldException ignored) {
            // last resort: try setName or similar
            // try setClusterId
            try {
                Method setIdAlt = scClass.getMethod("setClusterId", String.class);
                setIdAlt.invoke(sc, id);
                return sc;
            } catch (NoSuchMethodException ex) {
                throw new IllegalStateException("Cannot set id on SparkCluster; no known setter/field found");
            }
        }
    }

    /**
     * Helper to read id from a SparkCluster instance via getId() or field access.
     */
    private String getSparkClusterId(Object sc) throws Exception {
        Class<?> scClass = sc.getClass();
        try {
            Method getId = scClass.getMethod("getId");
            Object res = getId.invoke(sc);
            return res == null ? null : String.valueOf(res);
        } catch (NoSuchMethodException ignored) {
            // try field
        }
        try {
            Field idField = scClass.getDeclaredField("id");
            idField.setAccessible(true);
            Object val = idField.get(sc);
            return val == null ? null : String.valueOf(val);
        } catch (NoSuchFieldException ignored) {
            // fallback
            try {
                Method getIdAlt = scClass.getMethod("getClusterId");
                Object res = getIdAlt.invoke(sc);
                return res == null ? null : String.valueOf(res);
            } catch (NoSuchMethodException ex) {
                throw new IllegalStateException("Cannot read id from SparkCluster; no known getter/field found");
            }
        }
    }


    @Test
    public void testPrivateMerge_combinesAndDeduplicatesAndSorts() throws Exception {
        // Build three clusters: cm has "b","a"; db has "a","c"
        Object cmB = newSparkCluster("b");
        Object cmA = newSparkCluster("a");
        Object dbA = newSparkCluster("a");
        Object dbC = newSparkCluster("c");
        List<Object> cm = new ArrayList<>();
        cm.add(cmB);
        cm.add(cmA);
        List<Object> db = new ArrayList<>();
        db.add(dbA);
        db.add(dbC);
        // Access private static method ConfigUtil.merge(List, List)
        Method mergeMethod = null;
        for (Method m : ConfigUtil.class.getDeclaredMethods()) {
            if ("merge".equals(m.getName()) && m.getParameterCount() == 2) {
                mergeMethod = m;
                break;
            }
        }
        assertNotNull(mergeMethod, "Expected private method merge(List, List) to exist in ConfigUtil");
        mergeMethod.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Object> merged = (List<Object>) mergeMethod.invoke(null, cm, db);
        assertNotNull(merged);
        // Extract ids
        List<String> ids = new ArrayList<>();
        for (Object sc : merged) {
            ids.add(getSparkClusterId(sc));
        }
        // Expected unique sorted ids: a, b, c
        List<String> expected = Arrays.asList("a", "b", "c");
        assertEquals(expected, ids);
    }
}
