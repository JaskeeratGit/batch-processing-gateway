package com.apple.spark.util;

import java.lang.reflect.Method;
import java.util.Objects;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

public class KubernetesClusterAndNamespace_hashCode_3_0_Test_testHashCodeMatchesObjectsHash_withNullFields {


    @Test
    void testHashCodeMatchesObjectsHash_withNullFields() throws Exception {
        String masterUrl = null;
        String namespace = null;
        KubernetesClusterAndNamespace obj = new KubernetesClusterAndNamespace(masterUrl, namespace);
        int expected = Objects.hash(masterUrl, namespace);
        assertEquals(expected, obj.hashCode());
        Method hashCodeMethod = KubernetesClusterAndNamespace.class.getDeclaredMethod("hashCode");
        hashCodeMethod.setAccessible(true);
        assertEquals(expected, ((Integer) hashCodeMethod.invoke(obj)).intValue());
    }




}
