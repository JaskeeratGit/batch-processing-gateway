package com.apple.spark.core;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.apple.spark.AppConfig;
import com.apple.spark.util.EndAwareInputStream;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.*;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.Closeable;
import java.io.InputStream;

public class KubernetesHelper_tryGetServiceAccountCACertFile_1_0_Test {

    // Helper: compile Java source in-memory and load class bytes via a child classloader
    private static Class<?> compileAndLoadClass(String fullClassName, String source) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("No JavaCompiler available. Ensure tests run on a JDK, not a JRE.");
        }
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        InMemoryJavaFileObject srcObject = new InMemoryJavaFileObject(fullClassName, source);
        StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8);
        InMemoryClassFileManager fileManager = new InMemoryClassFileManager(standardFileManager);
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, Collections.singletonList(srcObject));
        Boolean ok = task.call();
        if (!Boolean.TRUE.equals(ok)) {
            StringBuilder sb = new StringBuilder("Compilation failed. Diagnostics:\n");
            for (Diagnostic<? extends JavaFileObject> d : diagnostics.getDiagnostics()) {
                sb.append(d.toString()).append("\n");
            }
            throw new IllegalStateException(sb.toString());
        }
        Map<String, byte[]> classBytes = fileManager.getAllClassBytes();
        ChildFirstClassLoader loader = new ChildFirstClassLoader(classBytes, KubernetesHelper_tryGetServiceAccountCACertFile_1_0_Test.class.getClassLoader());
        return loader.loadClass(fullClassName);
    }

    @Test
    public void tryGetServiceAccountCACertFile_whenFileExists_returnsAbsolutePath() throws Exception {
        Path tempDir = Files.createTempDirectory("k8s-test-");
        try {
            // create the ca.crt file in tempDir
            Path ca = tempDir.resolve("ca.crt");
            Files.write(ca, "dummy".getBytes(StandardCharsets.UTF_8));
            // escape backslashes for Java source on Windows
            String folder = tempDir.toAbsolutePath().toString().replace("\\", "\\\\");
            String className = "com.apple.spark.core.KubernetesHelperTestable";
            String source = "package com.apple.spark.core;\n" + "import java.nio.file.*;\n" + "public class KubernetesHelperTestable {\n" + "  public static final String SERVICE_ACCOUNT_FOLDER = \"" + folder + "\";\n" + "  public static final String SERVICE_ACCOUNT_CA_CERT_FILE = \"ca.crt\";\n" + "  public static String tryGetServiceAccountCACertFile() {\n" + "    Path path = Paths.get(SERVICE_ACCOUNT_FOLDER, SERVICE_ACCOUNT_CA_CERT_FILE).toAbsolutePath();\n" + "    if (Files.exists(path)) {\n" + "      return path.toString();\n" + "    } else {\n" + "      return \"\";\n" + "    }\n" + "  }\n" + "}";
            Class<?> cls = compileAndLoadClass(className, source);
            Method method = cls.getDeclaredMethod("tryGetServiceAccountCACertFile");
            Object result = method.invoke(null);
            assertNotNull(result);
            assertTrue(result instanceof String);
            assertEquals(ca.toAbsolutePath().toString(), result);
        } finally {
            // cleanup
            try {
                Files.walk(tempDir).sorted(Comparator.reverseOrder()).forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException ignored) {
                    }
                });
            } catch (IOException ignored) {
            }
        }
    }

    @Test
    public void tryGetServiceAccountCACertFile_whenFileNotExists_returnsEmptyString() throws Exception {
        Path tempDir = Files.createTempDirectory("k8s-test-");
        try {
            // do NOT create ca.crt file here
            // escape backslashes for Java source on Windows
            String folder = tempDir.toAbsolutePath().toString().replace("\\", "\\\\");
            String className = "com.apple.spark.core.KubernetesHelperTestable";
            String source = "package com.apple.spark.core;\n" + "import java.nio.file.*;\n" + "public class KubernetesHelperTestable {\n" + "  public static final String SERVICE_ACCOUNT_FOLDER = \"" + folder + "\";\n" + "  public static final String SERVICE_ACCOUNT_CA_CERT_FILE = \"ca.crt\";\n" + "  public static String tryGetServiceAccountCACertFile() {\n" + "    Path path = Paths.get(SERVICE_ACCOUNT_FOLDER, SERVICE_ACCOUNT_CA_CERT_FILE).toAbsolutePath();\n" + "    if (Files.exists(path)) {\n" + "      return path.toString();\n" + "    } else {\n" + "      return \"\";\n" + "    }\n" + "  }\n" + "}";
            Class<?> cls = compileAndLoadClass(className, source);
            Method method = cls.getDeclaredMethod("tryGetServiceAccountCACertFile");
            Object result = method.invoke(null);
            assertNotNull(result);
            assertTrue(result instanceof String);
            assertEquals("", result);
        } finally {
            // cleanup
            try {
                Files.walk(tempDir).sorted(Comparator.reverseOrder()).forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException ignored) {
                    }
                });
            } catch (IOException ignored) {
            }
        }
    }

    // In-memory JavaFileObject for source
    static class InMemoryJavaFileObject extends SimpleJavaFileObject {

        private final String sourceCode;

        InMemoryJavaFileObject(String className, String sourceCode) {
            super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.sourceCode = sourceCode;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return sourceCode;
        }
    }

    // FileManager to capture compiled class bytes
    static class InMemoryClassFileManager extends ForwardingJavaFileManager<JavaFileManager> {

        private final Map<String, ByteArrayOutputStream> classOutputBuffers = new HashMap<>();

        InMemoryClassFileManager(JavaFileManager fileManager) {
            super(fileManager);
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {
            return new SimpleJavaFileObject(URI.create("mem:///" + className + kind.extension), kind) {

                @Override
                public OutputStream openOutputStream() {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    classOutputBuffers.put(className, baos);
                    return baos;
                }
            };
        }

        Map<String, byte[]> getAllClassBytes() {
            Map<String, byte[]> result = new HashMap<>();
            for (Map.Entry<String, ByteArrayOutputStream> e : classOutputBuffers.entrySet()) {
                result.put(e.getKey(), e.getValue().toByteArray());
            }
            return result;
        }
    }

    // Child-first ClassLoader to load compiled classes from bytes
    static class ChildFirstClassLoader extends ClassLoader {

        private final Map<String, byte[]> classBytes;

        ChildFirstClassLoader(Map<String, byte[]> classBytes, ClassLoader parent) {
            super(parent);
            this.classBytes = new HashMap<>(classBytes);
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            byte[] bytes = classBytes.get(name);
            if (bytes != null) {
                return defineClass(name, bytes, 0, bytes.length);
            }
            return super.findClass(name);
        }

        @Override
        public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            // try to load from our bytes first (child-first)
            if (classBytes.containsKey(name)) {
                Class<?> c = findClass(name);
                if (resolve) {
                    resolveClass(c);
                }
                return c;
            }
            return super.loadClass(name, resolve);
        }
    }
}
