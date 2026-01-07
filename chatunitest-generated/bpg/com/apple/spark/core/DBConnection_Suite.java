package com.apple.spark.core;

import org.junit.runner.RunWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;

@RunWith(value = JUnitPlatform.class)
@SelectClasses(value = { DBConnection_executeSql_1_0_Test.class, DBConnection_close_2_0_Test.class })
public class DBConnection_Suite {
}
