/*
 * Copyright 2014. AppDynamics LLC and its affiliates.
 *  * All Rights Reserved.
 *  * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

 package com.appdynamics.extensions.couchbase.metrics.xdcr;

 import com.appdynamics.extensions.MetricWriteHelper;
 import com.appdynamics.extensions.conf.MonitorContext;
 import com.appdynamics.extensions.conf.MonitorContextConfiguration;
 import com.appdynamics.extensions.executorservice.MonitorExecutorService;
 import com.appdynamics.extensions.metrics.Metric;
 import com.appdynamics.extensions.yml.YmlReader;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.google.common.collect.Lists;
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
//  import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 
 public class IndividualXDCRBucketsTest {
 
     MonitorContextConfiguration contextConfiguration = Mockito.mock(MonitorContextConfiguration.class);
     MonitorContext context = Mockito.mock(MonitorContext.class);
     MetricWriteHelper metricWriteHelper = Mockito.mock(MetricWriteHelper.class);
     MonitorExecutorService executorService = Mockito.mock(MonitorExecutorService.class);
     CloseableHttpClient httpClient = Mockito.mock(CloseableHttpClient.class);
     CloseableHttpResponse response = Mockito.mock(CloseableHttpResponse.class);
     StatusLine statusLine = Mockito.mock(StatusLine.class);
     BasicHttpEntity entity;
     BasicHttpEntity entity1;
     Map<String, ?> conf;
     String serverURL;
 
     @Before
     public void init() throws IOException{
         conf = YmlReader.readFromFile(new File("src/test/resources/conf/config.yml"));
         entity = new BasicHttpEntity();
         entity.setContent(new FileInputStream("src/test/resources/json/xdcr/XDCRBucket.json"));
     }
 
     @Test
     public void xdcrMetricsTest() throws IOException{
         ArgumentCaptor<List> pathCaptor = ArgumentCaptor.forClass(List.class);
         CountDownLatch latch = new CountDownLatch(1);
 
         Mockito.when(contextConfiguration.getContext()).thenReturn(context);
         Mockito.when(context.getHttpClient()).thenReturn(httpClient);
         Mockito.when(context.getExecutorService()).thenReturn(executorService);
         Mockito.when(httpClient.execute(any(HttpGet.class))).thenReturn(response);
         Mockito.when(statusLine.getStatusCode()).thenReturn(200);
         Mockito.when(response.getStatusLine()).thenReturn(statusLine);
         Mockito.when(response.getEntity()).thenReturn(entity);
 
         List<Map<String, ?>> xdcrList = Lists.newArrayList();
         ObjectMapper mapper = new ObjectMapper();
         try (MockedStatic<IndividualXDCRBuckets> mocked = Mockito.mockStatic(IndividualXDCRBuckets.class)) {
             IndividualXDCRBuckets xdcrMetrics = new IndividualXDCRBuckets(contextConfiguration, metricWriteHelper, "cluster1", "localhost:8090", mapper.readValue(entity.getContent(), JsonNode.class), xdcrList, latch);
             xdcrMetrics.run();
         }
 
         verify(metricWriteHelper, times(1)).transformAndPrintMetrics(pathCaptor.capture());
         List<Metric> resultList = pathCaptor.getValue();
         Set<String> metricNames = Sets.newHashSet();
         metricNames.add("status");
         for(Metric metric : resultList){
             Assert.assertTrue(metricNames.contains(metric.getMetricName()));
             Assert.assertTrue(metric.getMetricValue().equalsIgnoreCase("1"));
         }
         Assert.assertTrue(resultList.size() == 1);
     }
 }