package com.apple.spark.core;

import org.junit.runner.RunWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;

@RunWith(value = JUnitPlatform.class)
@SelectClasses(value = { BPGStatsdConfig_flavor_3_0_Test.class, BPGStatsdConfig_port_5_0_Test.class, BPGStatsdConfig_get_1_0_Test.class, BPGStatsdConfig_enabled_2_0_Test.class, BPGStatsdConfig_host_4_0_Test.class })
public class BPGStatsdConfig_Suite {
}
