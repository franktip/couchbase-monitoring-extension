/*
 * Copyright 2014. AppDynamics LLC and its affiliates.
 *  * All Rights Reserved.
 *  * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

 package com.appdynamics.extensions.couchbase.metrics;

 import com.appdynamics.extensions.MetricWriteHelper;
 import com.appdynamics.extensions.conf.MonitorContextConfiguration;
 import com.appdynamics.extensions.metrics.Metric;
 import com.appdynamics.extensions.yml.YmlReader;
 import com.google.common.collect.Sets;
 import org.apache.http.StatusLine;
 import org.apache.http.client.methods.CloseableHttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.entity.BasicHttpEntity;
 import org.apache.http.impl.client.CloseableHttpClient;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.MockedStatic;
 import org.mockito.Mockito;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.CountDownLatch;
 
 public class QueryServiceMetricsTest{
 
     MonitorContextConfiguration contextConfiguration = Mockito.mock(MonitorContextConfiguration.class);
     MetricWriteHelper metricWriteHelper = Mockito.mock(MetricWriteHelper.class);
     CloseableHttpClient httpClient = Mockito.mock(CloseableHttpClient.class);
     CloseableHttpResponse response = Mockito.mock(CloseableHttpResponse.class);
     StatusLine statusLine = Mockito.mock(StatusLine.class);
     BasicHttpEntity entity;
     Map<String, ?> conf;
 
     @Before
     public void init() throws IOException{
         conf = YmlReader.readFromFile(new File("src/test/resources/conf/config.yml"));
         entity = new BasicHttpEntity();
         entity.setContent(new FileInputStream("src/test/resources/json/Query.json"));
     }
 
     @Test
     public void queryMetricsTest() throws IOException{
         ArgumentCaptor<List> pathCaptor = ArgumentCaptor.forClass(List.class);
         CountDownLatch latch = new CountDownLatch(1);
         Mockito.when(httpClient.execute(Mockito.any(HttpGet.class))).thenReturn(response);
         Mockito.when(statusLine.getStatusCode()).thenReturn(200);
         Mockito.when(response.getStatusLine()).thenReturn(statusLine);
         Mockito.when(response.getEntity()).thenReturn(entity);
         Map<String, ?> metricsMap = (Map<String, ?>)conf.get("metrics");
         List<Map<String, String>> serversList = (List<Map<String, String>>)conf.get("servers");
         try (MockedStatic<QueryServiceMetrics> mocked = Mockito.mockStatic(QueryServiceMetrics.class)) {
             QueryServiceMetrics queryServiceMetrics = new QueryServiceMetrics(contextConfiguration, metricWriteHelper, httpClient, serversList.get(0), "cluster1", metricsMap, latch);
             queryServiceMetrics.run();
         }
         Mockito.verify(metricWriteHelper, Mockito.times(1)).transformAndPrintMetrics(pathCaptor.capture());
         List<Metric> resultList = pathCaptor.getValue();
         Set<String> metricNames = Sets.newHashSet();
         metricNames.add("request.completed.count");
         metricNames.add("request.active.count");
         metricNames.add("request.per.sec.1min");
         metricNames.add("request.per.sec.5min");
         metricNames.add("request.per.sec.15min");
         metricNames.add("request_time.mean");
         metricNames.add("request_time.median");
         metricNames.add("request_time.80percentile");
         metricNames.add("request_time.95percentile");
         metricNames.add("request_time.99percentile");
         metricNames.add("request.prepared.percent");
         for(Metric metric : resultList){
             Assert.assertTrue(metricNames.contains(metric.getMetricName()));
         }
         Assert.assertTrue(resultList.size() == 11);
     }
 
 }