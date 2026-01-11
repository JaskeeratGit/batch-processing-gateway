package com.apple.spark.core;

import java.lang.reflect.Field;
import java.util.Objects;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

class NamespaceAndName_hashCode_5_0_Test_testHashCode_twoInstancesSameValues_haveSameHash {




    @Test
    void testHashCode_twoInstancesSameValues_haveSameHash() {
        NamespaceAndName a = new NamespaceAndName("ns", "nm");
        NamespaceAndName b = new NamespaceAndName();
        b.setNamespace("ns");
        b.setName("nm");
        assertEquals(a.hashCode(), b.hashCode());
        assertEquals(Objects.hash("ns", "nm"), a.hashCode());
    }


}
