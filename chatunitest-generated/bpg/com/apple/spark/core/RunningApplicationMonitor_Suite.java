package com.apple.spark.core;

import org.junit.runner.RunWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;

@RunWith(value = JUnitPlatform.class)
@SelectClasses(value = { RunningApplicationMonitor_deleteLongRunningApplications_2_0_Test.class, RunningApplicationMonitor_onUpdate_1_1_Test.class, RunningApplicationMonitor_getMaxRunningMillis_0_0_Test.class })
public class RunningApplicationMonitor_Suite {
}
