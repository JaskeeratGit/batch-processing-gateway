package com.apple.spark.core;

import java.lang.reflect.Field;
import java.util.Objects;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

class NamespaceAndName_hashCode_5_0_Test_testHashCode_parameterizedConstructor_nonNulls {


    @Test
    void testHashCode_parameterizedConstructor_nonNulls() {
        NamespaceAndName nn = new NamespaceAndName("namespace1", "name1");
        int expected = Objects.hash("namespace1", "name1");
        assertEquals(expected, nn.hashCode());
    }




}
