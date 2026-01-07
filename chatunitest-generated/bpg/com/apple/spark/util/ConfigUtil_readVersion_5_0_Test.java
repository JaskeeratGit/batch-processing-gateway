package com.apple.spark.util;

import java.io.BufferedWriter;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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

public class ConfigUtil_readVersion_5_0_Test {

    // Helper: compare field-wise equality of two instances of the same class
    private static boolean areFieldValuesEqual(Object a, Object b) throws Exception {
        if (a == null || b == null)
            return a == b;
        Class<?> cls = a.getClass();
        if (!cls.equals(b.getClass()))
            return false;
        for (Field f : cls.getDeclaredFields()) {
            f.setAccessible(true);
            Object va = f.get(a);
            Object vb = f.get(b);
            if (va == null && vb == null)
                continue;
            if (va == null ^ vb == null)
                return false;
            if (!va.equals(vb))
                return false;
        }
        return true;
    }

    @Test
    public void testReadVersion_whenVersionFilePresent_readsYaml() throws Exception {
        Class<?> cfgClass = Class.forName("com.apple.spark.util.ConfigUtil");
        // locate where the ConfigUtil.class is loaded from on disk
        URL classUrl = cfgClass.getResource("ConfigUtil.class");
        Assumptions.assumeTrue(classUrl != null, "ConfigUtil.class resource not found; skipping test");
        // we can only write a resource to the classpath root if the class was loaded from the filesystem
        String protocol = classUrl.getProtocol();
        Assumptions.assumeTrue("file".equals(protocol), "ConfigUtil.class not loaded from file protocol; skipping writable-resource test");
        // Derive classpath root (strip /com/apple/spark/util/ConfigUtil.class)
        String classPathSuffix = "/com/apple/spark/util/ConfigUtil.class";
        String fullPath = classUrl.getPath();
        int idx = fullPath.lastIndexOf(classPathSuffix);
        Assumptions.assumeTrue(idx > 0, "Cannot derive classpath root; skipping test");
        String classpathRoot = fullPath.substring(0, idx);
        Path rootDir = Path.of(classpathRoot);
        Assumptions.assumeTrue(Files.isDirectory(rootDir), "Classpath root is not a directory; skipping test");
        Path versionFile = rootDir.resolve("version.txt");
        // YAML content to map into VersionInfo fields (structure depends on VersionInfo POJO)
        String yaml = "version: \"1.2.3\"\n" + "description: \"test description\"\n" + "build: 123\n";
        // write version.txt
        Files.createDirectories(versionFile.getParent());
        try (BufferedWriter w = Files.newBufferedWriter(versionFile, StandardCharsets.UTF_8)) {
            w.write(yaml);
        }
        try {
            // invoke ConfigUtil.readVersion()
            Method readVersion = cfgClass.getMethod("readVersion");
            Object verInstance = readVersion.invoke(null);
            assertNotNull(verInstance, "readVersion returned null when version.txt present");
            // compare with default instance: expect at least one field differs from default
            Class<?> verClass = Class.forName("com.apple.spark.util.VersionInfo");
            Object defaultInst = verClass.getDeclaredConstructor().newInstance();
            boolean equal = areFieldValuesEqual(verInstance, defaultInst);
            assertFalse(equal, "Expected parsed VersionInfo to differ from default when version.txt present");
        } finally {
            // cleanup file
            try {
                Files.deleteIfExists(versionFile);
            } catch (Exception ignore) {
            }
        }
    }

    @Test
    public void testReadVersion_whenVersionFileMissing_returnsDefaultInstance() throws Exception {
        Class<?> cfgClass = Class.forName("com.apple.spark.util.ConfigUtil");
        URL classUrl = cfgClass.getResource("ConfigUtil.class");
        Assumptions.assumeTrue(classUrl != null, "ConfigUtil.class resource not found; skipping test");
        String protocol = classUrl.getProtocol();
        Assumptions.assumeTrue("file".equals(protocol), "ConfigUtil.class not loaded from file protocol; skipping writable-resource test");
        String classPathSuffix = "/com/apple/spark/util/ConfigUtil.class";
        String fullPath = classUrl.getPath();
        int idx = fullPath.lastIndexOf(classPathSuffix);
        Assumptions.assumeTrue(idx > 0, "Cannot derive classpath root; skipping test");
        String classpathRoot = fullPath.substring(0, idx);
        Path rootDir = Path.of(classpathRoot);
        Assumptions.assumeTrue(Files.isDirectory(rootDir), "Classpath root is not a directory; skipping test");
        Path versionFile = rootDir.resolve("version.txt");
        // ensure version.txt is absent
        Files.deleteIfExists(versionFile);
        // invoke readVersion
        Method readVersion = cfgClass.getMethod("readVersion");
        Object verInstance = readVersion.invoke(null);
        assertNotNull(verInstance, "readVersion returned null when version.txt missing");
        // compare with default instance: expect equality (method returns a fresh VersionInfo())
        Class<?> verClass = Class.forName("com.apple.spark.util.VersionInfo");
        Object defaultInst = verClass.getDeclaredConstructor().newInstance();
        boolean equal = areFieldValuesEqual(verInstance, defaultInst);
        assertTrue(equal, "Expected returned VersionInfo to equal default instance when version.txt missing");
    }
}
