package com.apple.spark.util;

import java.lang.reflect.Method;
import java.util.Objects;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

public class KubernetesClusterAndNamespace_hashCode_3_0_Test_testHashCodeVariousCombinations_matchObjectsHash {






    @Test
    void testHashCodeVariousCombinations_matchObjectsHash() throws Exception {
        String[][] combos = { { "u1", "n1" }, { "u1", null }, { null, "n1" }, { null, null }, { "", "" }, { "u-long-value", "n-long-value" } };
        Method hashCodeMethod = KubernetesClusterAndNamespace.class.getDeclaredMethod("hashCode");
        hashCodeMethod.setAccessible(true);
        for (String[] combo : combos) {
            String u = combo[0];
            String n = combo[1];
            KubernetesClusterAndNamespace obj = new KubernetesClusterAndNamespace(u, n);
            int expected = Objects.hash(u, n);
            assertEquals(expected, obj.hashCode());
            assertEquals(expected, ((Integer) hashCodeMethod.invoke(obj)).intValue());
        }
    }
}
