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

public class DBConnection_close_2_0_Test_testCloseWhenConnectionIsNull {

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
    public void testCloseWhenConnectionIsNull() throws Exception {
        DBConnection db = new DBConnection("jdbc:dummy");
        // ensure initially null
        assertNull(getConnectionField(db));
        // calling close() when connection is null should not throw and should leave it null
        db.close();
        assertNull(getConnectionField(db));
    }


}
