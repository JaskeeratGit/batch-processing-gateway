package com.apple.spark.core;

import java.lang.reflect.Field;
import java.util.Objects;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

class NamespaceAndName_hashCode_5_0_Test_testHashCode_viaSetters_oneNull_otherNonNull {



    @Test
    void testHashCode_viaSetters_oneNull_otherNonNull() {
        NamespaceAndName nn = new NamespaceAndName();
        nn.setNamespace(null);
        nn.setName("onlyName");
        int expected = Objects.hash(null, "onlyName");
        assertEquals(expected, nn.hashCode());
        nn.setNamespace("onlyNamespace");
        nn.setName(null);
        expected = Objects.hash("onlyNamespace", null);
        assertEquals(expected, nn.hashCode());
    }



}
