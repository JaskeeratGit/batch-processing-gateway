package com.apple.spark.core;

import org.junit.runner.RunWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;

@RunWith(value = JUnitPlatform.class)
@SelectClasses(value = { KubernetesHelper_tryGetServiceAccountCACertFile_1_0_Test.class, KubernetesHelper_normalizeLabelValue_8_0_Test.class, KubernetesHelper_closeQuietly_7_0_Test.class, KubernetesHelper_tryGetServiceAccountToken_2_0_Test.class, KubernetesHelper_tryGetServiceAccountNamespace_0_0_Test.class, KubernetesHelper_getK8sClient_4_0_Test.class })
public class KubernetesHelper_Suite {
}
