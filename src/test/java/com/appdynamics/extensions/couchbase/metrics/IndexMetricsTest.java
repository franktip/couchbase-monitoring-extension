/*
 * Copyright 2014. AppDynamics LLC and its affiliates.
 *  * All Rights Reserved.
 *  * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

 package com.appdynamics.extensions.couchbase.metrics;

 import com.appdynamics.extensions.MetricWriteHelper;
 import com.appdynamics.extensions.conf.MonitorContext;
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
 
 import static org.mockito.ArgumentMatchers.any;
 import static org.mockito.ArgumentMatchers.anyList;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 
 public class IndexMetricsTest {
 
     MonitorContextConfiguration contextConfiguration = Mockito.mock(MonitorContextConfiguration.class);
     MonitorContext context = Mockito.mock(MonitorContext.class);
     MetricWriteHelper metricWriteHelper = Mockito.mock(MetricWriteHelper.class);
     CloseableHttpClient httpClient = Mockito.mock(CloseableHttpClient.class);
     CloseableHttpResponse response = Mockito.mock(CloseableHttpResponse.class);
     StatusLine statusLine = Mockito.mock(StatusLine.class);
     BasicHttpEntity entity;
     Map<String, ?> conf;
     Map<String, ?> confWithIncludeFalse;
 
     @Before
     public void init() throws IOException {
         conf = YmlReader.readFromFile(new File("src/test/resources/conf/config.yml"));
         confWithIncludeFalse = YmlReader.readFromFile(new File("src/test/resources/conf/config_WithIncludeFalse.yml"));
         entity = new BasicHttpEntity();
         entity.setContent(new FileInputStream("src/test/resources/json/Index.json"));
     }
 
     @Test
     public void indexMetricsTest() throws IOException {
         ArgumentCaptor<List> pathCaptor = ArgumentCaptor.forClass(List.class);
         CountDownLatch latch = new CountDownLatch(1);
 
         try (MockedStatic<MonitorContextConfiguration> mockedContextConfig = Mockito.mockStatic(MonitorContextConfiguration.class);
              MockedStatic<MonitorContext> mockedContext = Mockito.mockStatic(MonitorContext.class);
              MockedStatic<CloseableHttpClient> mockedHttpClient = Mockito.mockStatic(CloseableHttpClient.class);
              MockedStatic<StatusLine> mockedStatusLine = Mockito.mockStatic(StatusLine.class);
              MockedStatic<BasicHttpEntity> mockedEntity = Mockito.mockStatic(BasicHttpEntity.class)) {
             mockedContextConfig.when(() -> Mockito.mock(MonitorContextConfiguration.class)).thenReturn(contextConfiguration);
             mockedContext.when(() -> Mockito.mock(MonitorContext.class)).thenReturn(context);
             mockedHttpClient.when(() -> Mockito.mock(CloseableHttpClient.class)).thenReturn(httpClient);
             mockedStatusLine.when(() -> Mockito.mock(StatusLine.class)).thenReturn(statusLine);
             mockedEntity.when(() -> new BasicHttpEntity()).thenReturn(entity);
 
             Mockito.when(httpClient.execute(any(HttpGet.class))).thenReturn(response);
             Mockito.when(statusLine.getStatusCode()).thenReturn(200);
             Mockito.when(response.getStatusLine()).thenReturn(statusLine);
             Mockito.when(response.getEntity()).thenReturn(entity);
 
             Map<String, ?> metricsMap = (Map<String, ?>) conf.get("metrics");
             IndexMetrics indexMetrics = new IndexMetrics(contextConfiguration, metricWriteHelper, "cluster1", "localhost:8090", metricsMap, latch);
             indexMetrics.run();
 
             verify(metricWriteHelper, times(1)).transformAndPrintMetrics(pathCaptor.capture());
             List<Metric> resultList = pathCaptor.getValue();
             Set<String> metricNames = Sets.newHashSet();
             metricNames.add("memorySnapshotInterval");
             metricNames.add("stableSnapshotInterval");
             metricNames.add("maxRollbackPoints");
             for (Metric metric : resultList) {
                 Assert.assertTrue(metricNames.contains(metric.getMetricName()));
             }
             Assert.assertTrue(resultList.size() == 3);
         }
     }
 
     @Test
     public void indexMetricsWithIncludeFalseTest() throws IOException {
         CountDownLatch latch = new CountDownLatch(1);
 
         try (MockedStatic<MonitorContextConfiguration> mockedContextConfig = Mockito.mockStatic(MonitorContextConfiguration.class);
              MockedStatic<MonitorContext> mockedContext = Mockito.mockStatic(MonitorContext.class);
              MockedStatic<CloseableHttpClient> mockedHttpClient = Mockito.mockStatic(CloseableHttpClient.class);
              MockedStatic<StatusLine> mockedStatusLine = Mockito.mockStatic(StatusLine.class);
              MockedStatic<BasicHttpEntity> mockedEntity = Mockito.mockStatic(BasicHttpEntity.class)) {
             mockedContextConfig.when(() -> Mockito.mock(MonitorContextConfiguration.class)).thenReturn(contextConfiguration);
             mockedContext.when(() -> Mockito.mock(MonitorContext.class)).thenReturn(context);
             mockedHttpClient.when(() -> Mockito.mock(CloseableHttpClient.class)).thenReturn(httpClient);
             mockedStatusLine.when(() -> Mockito.mock(StatusLine.class)).thenReturn(statusLine);
             mockedEntity.when(() -> new BasicHttpEntity()).thenReturn(entity);
 
             Mockito.when(httpClient.execute(any(HttpGet.class))).thenReturn(response);
             Mockito.when(statusLine.getStatusCode()).thenReturn(200);
             Mockito.when(response.getStatusLine()).thenReturn(statusLine);
             Mockito.when(response.getEntity()).thenReturn(entity);
 
             Map<String, ?> metricsMap = (Map<String, ?>) confWithIncludeFalse.get("metrics");
             IndexMetrics indexMetrics2 = new IndexMetrics(contextConfiguration, metricWriteHelper, "cluster1", "localhost:8090", metricsMap, latch);
             indexMetrics2.run();
 
             verify(metricWriteHelper, times(0)).transformAndPrintMetrics(anyList());
         }
     }
 }