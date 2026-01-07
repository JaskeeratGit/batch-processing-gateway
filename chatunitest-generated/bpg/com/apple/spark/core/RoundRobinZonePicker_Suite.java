package com.apple.spark.core;

import org.junit.runner.RunWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;

@RunWith(value = JUnitPlatform.class)
@SelectClasses(value = { RoundRobinZonePicker_update_0_0_Test.class, RoundRobinZonePicker_pick_1_0_Test.class })
public class RoundRobinZonePicker_Suite {
}
