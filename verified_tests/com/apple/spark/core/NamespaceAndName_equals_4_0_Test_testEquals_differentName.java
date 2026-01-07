package com.apple.spark.core;

import java.lang.reflect.Method;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Objects;

public class NamespaceAndName_equals_4_0_Test_testEquals_differentName {

    // Helper subclass to test the getClass() != o.getClass() branch
    static class SubNamespaceAndName extends NamespaceAndName {

        public SubNamespaceAndName(String namespace, String name) {
            super(namespace, name);
        }
    }






    @Test
    public void testEquals_differentName() {
        NamespaceAndName a = new NamespaceAndName("ns", "name1");
        NamespaceAndName b = new NamespaceAndName("ns", "name2");
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
    }



}
