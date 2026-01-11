package com.apple.spark.core;

import com.apple.spark.AppConfig;
import com.apple.spark.api.SubmitApplicationRequest;
import com.apple.spark.operator.SparkApplicationSpec;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static com.apple.spark.core.BatchSchedulerConstants.PLACEHOLDER_TIMEOUT_IN_SECONDS;
import static com.apple.spark.core.BatchSchedulerConstants.YUNIKORN_ROOT_QUEUE;
import static com.apple.spark.core.BatchSchedulerConstants.YUNIKORN_SPARK_DEFAULT_QUEUE;
import static com.apple.spark.core.Constants.*;
import static com.apple.spark.core.SparkConstants.CORE_LIMIT_RATIO;
import static com.apple.spark.core.SparkConstants.DRIVER_CPU_BUFFER_RATIO;
import static com.apple.spark.core.SparkConstants.DRIVER_MEM_BUFFER_RATIO;
import static com.apple.spark.core.SparkConstants.EXECUTOR_CPU_BUFFER_RATIO;
import static com.apple.spark.core.SparkConstants.EXECUTOR_MEM_BUFFER_RATIO;
import static com.apple.spark.core.SparkPodNodeAffinityHelper.createNodeAffinityForSparkPods;
import com.apple.spark.AppConfig.SparkCluster;
import com.apple.spark.operator.Affinity;
import com.apple.spark.operator.BatchSchedulerConfiguration;
import com.apple.spark.operator.DriverSpec;
import com.apple.spark.operator.ExecutorSpec;
import com.apple.spark.operator.NodeAffinity;
import com.apple.spark.operator.SparkUIConfiguration;
import com.apple.spark.operator.Volume;
import com.apple.spark.util.ExceptionUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import io.fabric8.kubernetes.api.model.PodDNSConfig;
import io.fabric8.kubernetes.api.model.PodDNSConfigOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationSubmissionHelper_populateEnv_6_0_Test {

    // Helper to instantiate a class by its canonical name
    private Object instantiate(String className) throws Exception {
        Class<?> cls = Class.forName(className);
        return cls.getDeclaredConstructor().newInstance();
    }

    // Helper to create an EnvVar instance; supports either (String,String) ctor or no-arg + setters setName/setValue
    private Object createEnvVar(String name, String value) throws Exception {
        Class<?> envCls = Class.forName("com.apple.spark.operator.EnvVar");
        // try (String, String) constructor first
        try {
            Constructor<?> c = envCls.getConstructor(String.class, String.class);
            return c.newInstance(name, value);
        } catch (NoSuchMethodException e) {
            // fallback: no-arg + setName/setValue
            Object env = envCls.getDeclaredConstructor().newInstance();
            try {
                Method setName = envCls.getMethod("setName", String.class);
                setName.invoke(env, name);
            } catch (NoSuchMethodException ignored) {
            }
            try {
                Method setValue = envCls.getMethod("setValue", String.class);
                setValue.invoke(env, value);
            } catch (NoSuchMethodException ignored) {
            }
            return env;
        }
    }

    // Helper to set a property (setter) on a target object via reflection
    private void callSetter(Object target, String setterName, Class<?> paramType, Object arg) throws Exception {
        Method m = target.getClass().getMethod(setterName, paramType);
        m.invoke(target, arg);
    }

    // Helper to get a property (getter) via reflection
    private Object callGetter(Object target, String getterName) throws Exception {
        Method m = target.getClass().getMethod(getterName);
        return m.invoke(target);
    }

    @Test
    public void testPopulateEnv_noSources_doesNothing() throws Exception {
        // sparkSpec with no driver/executor
        SparkApplicationSpec sparkSpec = new SparkApplicationSpec();
        // request with no driver/executor
        SubmitApplicationRequest request = new SubmitApplicationRequest();
        // sparkCluster with no driver/executor
        // AppConfig.SparkCluster is a nested class; instantiate via reflection to avoid compile-time dependency on inner class API shape
        Object sparkCluster = instantiate("com.apple.spark.AppConfig$SparkCluster");
        // invoke populateEnv
        ApplicationSubmissionHelper.populateEnv(sparkSpec, request, (AppConfig.SparkCluster) sparkCluster);
        // Since no sources provided, sparkSpec driver and executor should remain null
        assertNull(sparkSpec.getDriver(), "Driver should be null when no envs provided");
        assertNull(sparkSpec.getExecutor(), "Executor should be null when no envs provided");
    }

    @Test
    public void testPopulateEnv_clusterDriverAndExecutor_copiedToSpec() throws Exception {
        SparkApplicationSpec sparkSpec = new SparkApplicationSpec();
        // empty request
        SubmitApplicationRequest request = new SubmitApplicationRequest();
        // create cluster and its driver/executor specs
        Object sparkCluster = instantiate("com.apple.spark.AppConfig$SparkCluster");
        Object driverSpec = instantiate("com.apple.spark.operator.DriverSpec");
        Object executorSpec = instantiate("com.apple.spark.operator.ExecutorSpec");
        // create EnvVar instances
        Object drvEnv = createEnvVar("CLUSTER_DRV_KEY", "drvVal");
        Object execEnv = createEnvVar("CLUSTER_EXEC_KEY", "execVal");
        // set env lists on driver and executor via their setEnv(List) methods
        Method setDriverEnv = driverSpec.getClass().getMethod("setEnv", java.util.List.class);
        setDriverEnv.invoke(driverSpec, Arrays.asList(drvEnv));
        Method setExecutorEnv = executorSpec.getClass().getMethod("setEnv", java.util.List.class);
        setExecutorEnv.invoke(executorSpec, Arrays.asList(execEnv));
        // set driver and executor into sparkCluster
        Method setClusterDriver = sparkCluster.getClass().getMethod("setDriver", driverSpec.getClass().getInterfaces().length > 0 ? driverSpec.getClass().getInterfaces()[0] : driverSpec.getClass());
        // It's safer to search for setDriver method that accepts Object-compatible parameter
        boolean setDriverDone = false;
        for (Method m : sparkCluster.getClass().getMethods()) {
            if (m.getName().equals("setDriver")) {
                m.invoke(sparkCluster, driverSpec);
                setDriverDone = true;
                break;
            }
        }
        if (!setDriverDone) {
            // fallback: look for any setDriver method
            for (Method m : sparkCluster.getClass().getMethods()) {
                if (m.getName().equals("setDriver")) {
                    m.invoke(sparkCluster, driverSpec);
                    break;
                }
            }
        }
        boolean setExecDone = false;
        for (Method m : sparkCluster.getClass().getMethods()) {
            if (m.getName().equals("setExecutor")) {
                m.invoke(sparkCluster, executorSpec);
                setExecDone = true;
                break;
            }
        }
        if (!setExecDone) {
            for (Method m : sparkCluster.getClass().getMethods()) {
                if (m.getName().equals("setExecutor")) {
                    m.invoke(sparkCluster, executorSpec);
                    break;
                }
            }
        }
        // invoke populateEnv
        ApplicationSubmissionHelper.populateEnv(sparkSpec, request, (AppConfig.SparkCluster) sparkCluster);
        // Verify sparkSpec has driver with 1 env and executor with 1 env
        Object specDriver = sparkSpec.getDriver();
        Object specExecutor = sparkSpec.getExecutor();
        // getEnv via reflection
        Method getEnvDriver = specDriver.getClass().getMethod("getEnv");
        List<?> driverEnvList = (List<?>) getEnvDriver.invoke(specDriver);
        Method getEnvExec = specExecutor.getClass().getMethod("getEnv");
        List<?> execEnvList = (List<?>) getEnvExec.invoke(specExecutor);
        assertEquals(1, driverEnvList.size(), "Driver env from cluster should be copied");
        assertEquals(1, execEnvList.size(), "Executor env from cluster should be copied");
    }

    @Test
    public void testPopulateEnv_allSources_combinesClusterAndRequestEnvs() throws Exception {
        SparkApplicationSpec sparkSpec = new SparkApplicationSpec();
        SubmitApplicationRequest request = new SubmitApplicationRequest();
        // create request driver and executor specs
        Object reqDriver = instantiate("com.apple.spark.operator.DriverSpec");
        Object reqExecutor = instantiate("com.apple.spark.operator.ExecutorSpec");
        Object reqDrvEnv = createEnvVar("REQ_DRV_KEY", "reqDrvVal");
        Object reqExecEnv = createEnvVar("REQ_EXEC_KEY", "reqExecVal");
        // set env lists on request driver/executor
        Method setReqDriverEnv = reqDriver.getClass().getMethod("setEnv", java.util.List.class);
        setReqDriverEnv.invoke(reqDriver, Arrays.asList(reqDrvEnv));
        Method setReqExecutorEnv = reqExecutor.getClass().getMethod("setEnv", java.util.List.class);
        setReqExecutorEnv.invoke(reqExecutor, Arrays.asList(reqExecEnv));
        // set into request
        boolean setReqDriverDone = false;
        for (Method m : request.getClass().getMethods()) {
            if (m.getName().equals("setDriver")) {
                m.invoke(request, reqDriver);
                setReqDriverDone = true;
                break;
            }
        }
        if (!setReqDriverDone) {
            // fallback: directly call setDriver if present
            try {
                Method m = request.getClass().getMethod("setDriver", reqDriver.getClass());
                m.invoke(request, reqDriver);
            } catch (NoSuchMethodException ignored) {
            }
        }
        for (Method m : request.getClass().getMethods()) {
            if (m.getName().equals("setExecutor")) {
                m.invoke(request, reqExecutor);
                break;
            }
        }
        // create cluster with its own driver/executor envs
        Object sparkCluster = instantiate("com.apple.spark.AppConfig$SparkCluster");
        Object clusterDriver = instantiate("com.apple.spark.operator.DriverSpec");
        Object clusterExecutor = instantiate("com.apple.spark.operator.ExecutorSpec");
        Object clusterDrvEnv = createEnvVar("CLUSTER_DRV_KEY2", "clusterDrvVal2");
        Object clusterExecEnv = createEnvVar("CLUSTER_EXEC_KEY2", "clusterExecVal2");
        Method setClusterDriverEnv = clusterDriver.getClass().getMethod("setEnv", java.util.List.class);
        setClusterDriverEnv.invoke(clusterDriver, Arrays.asList(clusterDrvEnv));
        Method setClusterExecEnv = clusterExecutor.getClass().getMethod("setEnv", java.util.List.class);
        setClusterExecEnv.invoke(clusterExecutor, Arrays.asList(clusterExecEnv));
        // set cluster driver/executor into sparkCluster
        for (Method m : sparkCluster.getClass().getMethods()) {
            if (m.getName().equals("setDriver")) {
                m.invoke(sparkCluster, clusterDriver);
            } else if (m.getName().equals("setExecutor")) {
                m.invoke(sparkCluster, clusterExecutor);
            }
        }
        // invoke populateEnv
        ApplicationSubmissionHelper.populateEnv(sparkSpec, request, (AppConfig.SparkCluster) sparkCluster);
        // Verify sparkSpec driver env list has 2 items (cluster + request) and executor likewise
        Object specDriver = sparkSpec.getDriver();
        Object specExecutor = sparkSpec.getExecutor();
        Method getEnvDriver = specDriver.getClass().getMethod("getEnv");
        List<?> driverEnvList = (List<?>) getEnvDriver.invoke(specDriver);
        Method getEnvExec = specExecutor.getClass().getMethod("getEnv");
        List<?> execEnvList = (List<?>) getEnvExec.invoke(specExecutor);
        assertEquals(2, driverEnvList.size(), "Driver env should contain both cluster and request entries");
        assertEquals(2, execEnvList.size(), "Executor env should contain both cluster and request entries");
    }
}
