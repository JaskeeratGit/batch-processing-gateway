package com.apple.spark.core;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.sql.DriverManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBConnection_executeSql_1_0_Test {

    private DBConnection dbConnection;

    @BeforeEach
    public void setup() {
        dbConnection = new DBConnection("jdbc:placeholder");
    }

    @Test
    public void testExecuteSql_success_invokesStatementExecute() throws Exception {
        List<String> executedSql = new ArrayList<>();
        Connection connProxy = createConnectionProxy(/* isClosedReturn= */
        false, /* isClosedThrow= */
        false, /* statementExecuteThrow= */
        false, executedSql);
        setPrivateConnection(dbConnection, connProxy);
        // Should not throw
        dbConnection.executeSql("SELECT 1");
        assertEquals(1, executedSql.size(), "Statement.execute should have been called once");
        assertEquals("SELECT 1", executedSql.get(0));
    }

    @Test
    public void testExecuteSql_statementThrowsSQLException_propagates() throws Exception {
        Connection connProxy = createConnectionProxy(/* isClosedReturn= */
        false, /* isClosedThrow= */
        false, /* statementExecuteThrow= */
        true, /* executedSql= */
        null);
        setPrivateConnection(dbConnection, connProxy);
        assertThrows(SQLException.class, () -> dbConnection.executeSql("UPDATE X SET Y=1"));
    }

    @Test
    public void testExecuteSql_existingConnectionClosed_triggersDriverManagerAndWrapsFailure() throws Exception {
        // Configure connection such that isClosed() returns true so getConnection() will try to create a new one via DriverManager
        // DriverManager.getConnection(...) will fail (no driver/invalid URL) and getConnection() wraps it into RuntimeException.
        Connection connProxy = createConnectionProxy(/* isClosedReturn= */
        true, /* isClosedThrow= */
        false, /* statementExecuteThrow= */
        false, /* executedSql= */
        null);
        setPrivateConnection(dbConnection, connProxy);
        // Expect a RuntimeException from getConnection attempt to create a new connection
        assertThrows(RuntimeException.class, () -> dbConnection.executeSql("SELECT 2"));
    }

    // Helper: set the private volatile field 'connection' in DBConnection via reflection
    private static void setPrivateConnection(DBConnection instance, Connection connection) throws Exception {
        Field f = DBConnection.class.getDeclaredField("connection");
        f.setAccessible(true);
        f.set(instance, connection);
    }

    // Helper factory to create a Connection proxy with configurable behavior.
    // - isClosedReturn: what isClosed() should return (when isClosedThrow is false)
    // - isClosedThrow: whether isClosed() should throw a RuntimeException
    // - statementExecuteThrow: whether Statement.execute(sql) should throw SQLException
    // - executedSql: list to capture executed SQL strings (can be null if not needed)
    private static Connection createConnectionProxy(boolean isClosedReturn, boolean isClosedThrow, boolean statementExecuteThrow, List<String> executedSql) {
        InvocationHandler connHandler = new InvocationHandler() {

            boolean closedCalled = false;

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String name = method.getName();
                if ("isClosed".equals(name) && method.getParameterCount() == 0) {
                    if (isClosedThrow) {
                        throw new RuntimeException("isClosed failure");
                    }
                    return isClosedReturn;
                }
                if ("close".equals(name) && method.getParameterCount() == 0) {
                    closedCalled = true;
                    return null;
                }
                if ("createStatement".equals(name)) {
                    // Return a Statement proxy
                    InvocationHandler stmtHandler = new InvocationHandler() {

                        @Override
                        public Object invoke(Object stmtProxy, Method stmtMethod, Object[] stmtArgs) throws Throwable {
                            String stmtName = stmtMethod.getName();
                            if ("execute".equals(stmtName) && stmtArgs != null && stmtArgs.length >= 1 && stmtArgs[0] instanceof String) {
                                String sql = (String) stmtArgs[0];
                                if (statementExecuteThrow) {
                                    throw new SQLException("execute failed");
                                }
                                if (executedSql != null) {
                                    executedSql.add(sql);
                                }
                                // Statement.execute returns boolean; we return true
                                return true;
                            }
                            // For methods that return int (like executeUpdate) or others, provide basic defaults
                            if ("close".equals(stmtName)) {
                                return null;
                            }
                            // Default simple return values to satisfy possible reflective calls: null or 0/false
                            Class<?> returnType = stmtMethod.getReturnType();
                            if (returnType == boolean.class)
                                return false;
                            if (returnType == int.class)
                                return 0;
                            return null;
                        }
                    };
                    return Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class[] { Statement.class }, stmtHandler);
                }
                // Basic defaults for other Connection methods
                Class<?> returnType = method.getReturnType();
                if (returnType == boolean.class)
                    return false;
                if (returnType == int.class)
                    return 0;
                return null;
            }
        };
        return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class[] { Connection.class }, connHandler);
    }
}
