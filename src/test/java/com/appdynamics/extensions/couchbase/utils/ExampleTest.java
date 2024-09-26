package com.appdynamics.extensions.couchbase.utils;

/**
 * Test illustrating use of PowerMockito.mockStatic() prior to migration
 */

import org.junit.Assert;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
   
 class ExampleClass {
     public static String buildMetricPath(final String metricPrefix, final String... metricTokens) {
         return "Example";
     }
 }
 
 public class ExampleTest {
 
     @Test
     public void getMetricsFromArrayTest() {
        try (MockedStatic<ExampleClass> mocked =  Mockito.mockStatic(ExampleClass.class)){
            mocked.when(() -> ExampleClass.buildMetricPath(Mockito.anyString(), Mockito.anyString())).thenReturn("Mocked!");
            String path = ExampleClass.buildMetricPath("foo","bar");
            System.out.println(path);
            Assert.assertTrue(path.equals("Mocked!"));
        }
     }
 }
 