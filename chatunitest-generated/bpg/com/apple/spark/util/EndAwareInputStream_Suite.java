package com.apple.spark.util;

import org.junit.runner.RunWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;

@RunWith(value = JUnitPlatform.class)
@SelectClasses(value = { EndAwareInputStream_read_2_0_Test.class, EndAwareInputStream_markSupported_8_0_Test.class, EndAwareInputStream_mark_6_0_Test.class, EndAwareInputStream_available_4_0_Test.class })
public class EndAwareInputStream_Suite {
}
