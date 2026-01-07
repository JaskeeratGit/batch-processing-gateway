package com.apple.spark.util;

import java.lang.reflect.Method;
import java.util.Objects;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

public class KubernetesClusterAndNamespace_hashCode_3_0_Test_testHashCodeConsistentAcrossCalls {



    @Test
    void testHashCodeConsistentAcrossCalls() {
        KubernetesClusterAndNamespace obj = new KubernetesClusterAndNamespace("u", "n");
        int first = obj.hashCode();
        int second = obj.hashCode();
        int third = obj.hashCode();
        assertEquals(first, second);
        assertEquals(first, third);
    }



}
