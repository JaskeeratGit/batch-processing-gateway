package com.apple.spark.core;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBConnection_close_2_0_Test_testCloseWhenConnectionClosesSuccessfully {

    // Helper to set the private 'connection' field via reflection
    private void setConnectionField(DBConnection dbConnection, Connection conn) throws Exception {
        Field f = DBConnection.class.getDeclaredField("connection");
        f.setAccessible(true);
        f.set(dbConnection, conn);
    }

    // Helper to get the private 'connection' field via reflection
    private Connection getConnectionField(DBConnection dbConnection) throws Exception {
        Field f = DBConnection.class.getDeclaredField("connection");
        f.setAccessible(true);
        return (Connection) f.get(dbConnection);
    }


    @Test
    public void testCloseWhenConnectionClosesSuccessfully() throws Exception {
        DBConnection db = new DBConnection("jdbc:dummy");
        // create a proxy Connection whose close() method sets a flag
        final boolean[] closed = new boolean[] { false };
        InvocationHandler handler = new InvocationHandler() {

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if ("close".equals(method.getName())) {
                    closed[0] = true;
                    return null;
                }
                // Return reasonable defaults for other methods
                Class<?> returnType = method.getReturnType();
                if (returnType == boolean.class)
                    return false;
                if (returnType == byte.class)
                    return (byte) 0;
                if (returnType == short.class)
                    return (short) 0;
                if (returnType == int.class)
                    return 0;
                if (returnType == long.class)
                    return 0L;
                if (returnType == float.class)
                    return 0.0f;
                if (returnType == double.class)
                    return 0.0d;
                return null;
            }
        };
        Connection proxyConn = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class[] { Connection.class }, handler);
        // inject the proxy into the private field
        setConnectionField(db, proxyConn);
        // preconditions
        assertNotNull(getConnectionField(db));
        assertFalse(closed[0]);
        // invoke close() - should call proxy.close(), set closed flag, and set field to null
        db.close();
        assertTrue(closed[0], "Underlying Connection.close() should have been invoked");
        assertNull(getConnectionField(db), "DBConnection.connection should be set to null after successful close");
    }

}
