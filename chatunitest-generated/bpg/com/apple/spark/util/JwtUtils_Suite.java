package com.apple.spark.util;

import org.junit.runner.RunWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;

@RunWith(value = JUnitPlatform.class)
@SelectClasses(value = { JwtUtils_verifyToken_2_0_Test.class, JwtUtils_createToken_1_0_Test.class })
public class JwtUtils_Suite {
}
