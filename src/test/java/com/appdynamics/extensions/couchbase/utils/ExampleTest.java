package com.appdynamics.extensions.couchbase.utils;

/**
 * Test illustrating use of PowerMockito.mockStatic() prior to migration
 */

 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mockito;
 import org.powermock.api.mockito.PowerMockito;
 import org.powermock.core.classloader.annotations.PowerMockIgnore;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
   
 class ExampleClass {
     public static String buildMetricPath(final String metricPrefix, final String... metricTokens) {
         return "Example";
     }
 }
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest(ExampleClass.class)
 @PowerMockIgnore({ "javax.net.ssl.*" })
 public class ExampleTest {
 
     @Test
     public void getMetricsFromArrayTest() {
         PowerMockito.mockStatic(ExampleClass.class);
         Mockito.when(ExampleClass.buildMetricPath(Mockito.anyString(),Mockito.anyString())).thenReturn("Mocked!");
         String path = ExampleClass.buildMetricPath("foo","bar");
         Assert.assertTrue(path.equals("Mocked!"));
     }
 }
 