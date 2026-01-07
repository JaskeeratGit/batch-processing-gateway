package com.apple.spark.core;

import java.lang.reflect.Field;
import java.util.Objects;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

class NamespaceAndName_hashCode_5_0_Test_testHashCode_differentValues_expectedDifferentHash {





    @Test
    void testHashCode_differentValues_expectedDifferentHash() {
        NamespaceAndName a = new NamespaceAndName("nsA", "nmA");
        NamespaceAndName b = new NamespaceAndName("nsB", "nmB");
        int expectedA = Objects.hash("nsA", "nmA");
        int expectedB = Objects.hash("nsB", "nmB");
        assertEquals(expectedA, a.hashCode());
        assertEquals(expectedB, b.hashCode());
        assertNotEquals(a.hashCode(), b.hashCode());
    }

}
