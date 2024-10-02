/*
 * Copyright 2014. AppDynamics LLC and its affiliates.
 *  * All Rights Reserved.
 *  * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

 package com.appdynamics.extensions.couchbase.utils;

 import com.appdynamics.extensions.metrics.Metric;
 import com.appdynamics.extensions.util.MetricPathUtils;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import org.junit.Assert;
 import org.junit.Test;
 import org.mockito.MockedStatic;
 import org.mockito.Mockito;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 
 import static com.appdynamics.extensions.couchbase.utils.JsonUtils.getMetrics;
 import static com.appdynamics.extensions.couchbase.utils.JsonUtils.getMetricsFromArray;
 
 /**
  * Created by venkata.konala on 9/28/17.*/
 public class ConstantsTest {
 
     @Test
     public void getMetricsTest() throws IOException {
         try (MockedStatic<MetricPathUtils> mocked = Mockito.mockStatic(MetricPathUtils.class)) {
             List<Map<String, ?>> metricsList = Lists.newArrayList();
             Map<String, ?> metricsMap = Maps.newHashMap();
             metricsMap.put("total", null);
             metricsList.add(metricsMap);
             ObjectMapper mapper = new ObjectMapper();
             JsonNode metricsNode = mapper.readValue(new File("src/test/resources/json/constants/ConstantsMetricsNode.json"), JsonNode.class);
             mocked.when(() -> MetricPathUtils.buildMetricPath(Mockito.anyString(), Mockito.anyString())).thenReturn("Custom json|Redis");
             List<Metric> resultMetricList = getMetrics("Custom json|Redis", metricsList, metricsNode);
             Assert.assertTrue(resultMetricList.size() == 1);
             Assert.assertTrue(resultMetricList.listIterator().next().getMetricName().equals("total"));
             Assert.assertTrue(resultMetricList.listIterator().next().getMetricValue().equals("2095882240"));
         }
     }
 
     @Test
     public void getMetricsFromArrayTest() throws IOException {
         try (MockedStatic<MetricPathUtils> mocked = Mockito.mockStatic(MetricPathUtils.class)) {
             List<Map<String, ?>> metricsList = Lists.newArrayList();
             Map<String, ?> metricsMap = Maps.newHashMap();
             metricsMap.put("timestamp", null);
             metricsList.add(metricsMap);
             ObjectMapper mapper = new ObjectMapper();
             JsonNode metricsNode = mapper.readValue(new File("src/test/resources/json/constants/ConstantsMetricNodeWithArray.json"), JsonNode.class);
             mocked.when(() -> MetricPathUtils.buildMetricPath(Mockito.anyString(), Mockito.anyString())).thenReturn("Custom json|Redis");
             List<Metric> resultMetricList = getMetricsFromArray("Custom json", "Redis", metricsList, metricsNode);
             Assert.assertTrue(resultMetricList.size() == 1);
             Assert.assertTrue(resultMetricList.listIterator().next().getMetricName().equals("timestamp"));
             Assert.assertTrue(resultMetricList.listIterator().next().getMetricValue().equals("1506634199329"));
         }
     }
 }