package com.apple.spark.util;

import com.apple.spark.api.SubmitApplicationRequest;
import com.apple.spark.operator.DriverSpec;
import com.apple.spark.operator.ExecutorSpec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.apple.spark.operator.SparkApplicationSpec;

class CustomSerDe_submitRequestToNonSensitiveJson_0_0_Test_testDriverAndExecutorEnvRemovedAndQueueTokenMasked {

    @Test
    void testDriverAndExecutorEnvRemovedAndQueueTokenMasked() throws Exception {
        // Prepare SubmitApplicationRequest with driver/env, executor/env and a queue token
        SubmitApplicationRequest req = new SubmitApplicationRequest();
        // Create driver and executor specs and give them non-null env lists
        DriverSpec driver = new DriverSpec();
        ExecutorSpec executor = new ExecutorSpec();
        // We don't rely on concrete EnvVar implementation here; use raw lists to avoid generic issues.
        @SuppressWarnings({ "unchecked", "rawtypes" })
        List envList = Arrays.asList(Collections.singletonMap("ENV_KEY", "ENV_VALUE"), Collections.singletonMap("ANOTHER_KEY", "ANOTHER_VALUE"));
        // Set env lists
        driver.setEnv(envList);
        executor.setEnv(envList);
        req.setDriver(driver);
        req.setExecutor(executor);
        req.setQueueToken("super-secret-token");
        // Call the focal method
        String resultJson = CustomSerDe.submitRequestToNonSensitiveJson(req);
        // Parse result and assert driver/env and executor/env are removed and queueToken is masked
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(resultJson);
        // queueToken should be present and masked as "***"
        JsonNode queueTokenNode = root.get("queueToken");
        assertNotNull(queueTokenNode, "queueToken field should be present");
        assertEquals("***", queueTokenNode.asText(), "queueToken should be masked");
        // driver should be present but env should be absent (nulls are omitted by JsonInclude.NON_NULL)
        JsonNode driverNode = root.get("driver");
        assertNotNull(driverNode, "driver node should be present");
        assertFalse(driverNode.has("env"), "driver.env should be removed (not present in JSON)");
        // executor should be present but env should be absent
        JsonNode executorNode = root.get("executor");
        assertNotNull(executorNode, "executor node should be present");
        assertFalse(executorNode.has("env"), "executor.env should be removed (not present in JSON)");
    }

}
