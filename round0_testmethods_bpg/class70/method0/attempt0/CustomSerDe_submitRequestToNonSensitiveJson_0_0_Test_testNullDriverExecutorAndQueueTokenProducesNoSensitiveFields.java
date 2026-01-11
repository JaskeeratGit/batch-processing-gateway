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

class CustomSerDe_submitRequestToNonSensitiveJson_0_0_Test_testNullDriverExecutorAndQueueTokenProducesNoSensitiveFields {


    @Test
    void testNullDriverExecutorAndQueueTokenProducesNoSensitiveFields() throws Exception {
        // Prepare SubmitApplicationRequest with null driver, executor and queueToken
        SubmitApplicationRequest req = new SubmitApplicationRequest();
        req.setDriver(null);
        req.setExecutor(null);
        req.setQueueToken(null);
        // Call method
        String resultJson = CustomSerDe.submitRequestToNonSensitiveJson(req);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(resultJson);
        // driver, executor and queueToken should not be present in the output JSON
        assertFalse(root.has("driver"), "driver should not be present when null");
        assertFalse(root.has("executor"), "executor should not be present when null");
        assertFalse(root.has("queueToken"), "queueToken should not be present when null");
        // Also ensure result is valid JSON
        assertNotNull(root);
    }
}
