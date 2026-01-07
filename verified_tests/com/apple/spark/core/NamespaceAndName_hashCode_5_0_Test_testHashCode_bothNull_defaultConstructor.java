package com.apple.spark.core;

import java.lang.reflect.Field;
import java.util.Objects;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

class NamespaceAndName_hashCode_5_0_Test_testHashCode_bothNull_defaultConstructor {

    @Test
    void testHashCode_bothNull_defaultConstructor() {
        NamespaceAndName nn = new NamespaceAndName();
        int expected = Objects.hash(null, null);
        assertEquals(expected, nn.hashCode());
    }





}
