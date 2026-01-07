package com.apple.spark.core;

import java.lang.reflect.Method;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Objects;

public class NamespaceAndName_equals_4_0_Test_testEquals_nullArgument {

    // Helper subclass to test the getClass() != o.getClass() branch
    static class SubNamespaceAndName extends NamespaceAndName {

        public SubNamespaceAndName(String namespace, String name) {
            super(namespace, name);
        }
    }


    @Test
    public void testEquals_nullArgument() throws Exception {
        NamespaceAndName a = new NamespaceAndName("ns", "name");
        // direct call
        assertFalse(a.equals(null));
        // reflective invocation with explicit null argument
        Method equalsMethod = NamespaceAndName.class.getDeclaredMethod("equals", Object.class);
        Boolean result = (Boolean) equalsMethod.invoke(a, new Object[] { null });
        assertFalse(result);
    }







}
