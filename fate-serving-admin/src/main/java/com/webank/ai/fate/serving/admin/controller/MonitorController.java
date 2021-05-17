/*
 * Copyright 2019 The FATE Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.ai.fate.serving.admin.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.protobuf.ByteString;
import com.webank.ai.fate.api.networking.common.CommonServiceGrpc;
import com.webank.ai.fate.api.networking.common.CommonServiceProto;
import com.webank.ai.fate.api.serving.InferenceServiceGrpc;
import com.webank.ai.fate.api.serving.InferenceServiceProto;
import com.webank.ai.fate.serving.admin.services.ComponentService;
import com.webank.ai.fate.serving.common.flow.JvmInfo;
import com.webank.ai.fate.serving.common.flow.MetricNode;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.GrpcConnectionPool;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import com.webank.ai.fate.serving.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.exceptions.RemoteRpcException;
import com.webank.ai.fate.serving.core.exceptions.SysException;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import com.webank.ai.fate.serving.core.utils.NetUtils;
import com.webank.ai.fate.serving.core.utils.ThreadPoolUtil;
import io.grpc.ManagedChannel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@RequestMapping("/api")
@RestController
public class MonitorController {

    GrpcConnectionPool grpcConnectionPool = GrpcConnectionPool.getPool();
    @Autowired
    ComponentService componentService;
    Map<String,Object> healthRecord;
    private static ThreadPoolExecutor executor = ThreadPoolUtil.newThreadPoolExecutor();

    @GetMapping("/monitor/queryJvm")
    public ReturnResult queryJvmData(String host, int port) {
        CommonServiceGrpc.CommonServiceBlockingStub blockingStub = getMonitorServiceBlockStub(host, port);
        blockingStub = blockingStub.withDeadlineAfter(MetaInfo.PROPERTY_GRPC_TIMEOUT, TimeUnit.MILLISECONDS);
        CommonServiceProto.QueryJvmInfoRequest.Builder builder = CommonServiceProto.QueryJvmInfoRequest.newBuilder();
        CommonServiceProto.CommonResponse commonResponse = blockingStub.queryJvmInfo(builder.build());
        List<JvmInfo> resultList = Lists.newArrayList();
        if (commonResponse.getData() != null && !commonResponse.getData().toStringUtf8().equals("null")) {
            List<JvmInfo> resultData = JsonUtil.json2List(commonResponse.getData().toStringUtf8(), new TypeReference<List<JvmInfo>>() {
            });

            if (resultData != null) {
                resultList = resultData;
            }

            resultList = resultList.stream()
                    .sorted(((o1, o2) -> o1.getTimestamp() == o2.getTimestamp() ? 0 : ((o1.getTimestamp() - o2.getTimestamp()) > 0 ? 1 : -1)))
                    .collect(Collectors.toList());
        }
        Map map = Maps.newHashMap();
        map.put("total", resultList.size());
        map.put("rows", resultList);
        return ReturnResult.build(StatusCode.SUCCESS, Dict.SUCCESS, map);
    }

    @GetMapping("/monitor/query")
    public ReturnResult queryMonitorData(String host, int port, String source) {
        CommonServiceGrpc.CommonServiceBlockingStub blockingStub = getMonitorServiceBlockStub(host, port);
        blockingStub = blockingStub.withDeadlineAfter(MetaInfo.PROPERTY_GRPC_TIMEOUT, TimeUnit.MILLISECONDS);
        CommonServiceProto.QueryMetricRequest.Builder builder = CommonServiceProto.QueryMetricRequest.newBuilder();

        long now = System.currentTimeMillis();
        builder.setBeginMs(now - 15000);
        builder.setEndMs(now);
        if (StringUtils.isNotBlank(source)) {
            builder.setSource(source);
        }
        builder.setType(CommonServiceProto.MetricType.INTERFACE);
        CommonServiceProto.CommonResponse commonResponse = blockingStub.queryMetrics(builder.build());
        List<MetricNode> metricNodes = Lists.newArrayList();
        if (commonResponse.getData() != null && !commonResponse.getData().toStringUtf8().equals("null")) {
            List<MetricNode> resultData = JsonUtil.json2List(commonResponse.getData().toStringUtf8(), new TypeReference<List<MetricNode>>() {
            });
            if (resultData != null) {
                metricNodes = resultData;
            }
        }
        metricNodes = metricNodes.stream()
                .sorted(((o1, o2) -> o1.getTimestamp() == o2.getTimestamp() ? 0 : ((o1.getTimestamp() - o2.getTimestamp()) > 0 ? 1 : -1)))
                .collect(Collectors.toList());

        Map<String, Object> dataMap = Maps.newHashMap();
        if (metricNodes != null) {
            metricNodes.forEach(metricNode -> {
                List<MetricNode> nodes = (List<MetricNode>) dataMap.get(metricNode.getResource());
                if (nodes == null) {
                    nodes = Lists.newArrayList();
                }
                nodes.add(metricNode);
                dataMap.put(metricNode.getResource(), nodes);
            });
        }
        return ReturnResult.build(StatusCode.SUCCESS, Dict.SUCCESS, dataMap);
    }


    @GetMapping("/monitor/queryModel")
    public ReturnResult queryModelMonitorData(String host, int port, String source) {
        Preconditions.checkArgument(StringUtils.isNotBlank(source), "parameter source is blank");
        CommonServiceGrpc.CommonServiceBlockingStub blockingStub = getMonitorServiceBlockStub(host, port);
        blockingStub = blockingStub.withDeadlineAfter(MetaInfo.PROPERTY_GRPC_TIMEOUT, TimeUnit.MILLISECONDS);
        CommonServiceProto.QueryMetricRequest.Builder builder = CommonServiceProto.QueryMetricRequest.newBuilder();
        long now = System.currentTimeMillis();
        builder.setBeginMs(now - 15000);
        builder.setEndMs(now);
        if (StringUtils.isNotBlank(source)) {
            builder.setSource(source);
        }
        builder.setType(CommonServiceProto.MetricType.MODEL);
        CommonServiceProto.CommonResponse commonResponse = blockingStub.queryMetrics(builder.build());
        List<MetricNode> metricNodes = Lists.newArrayList();
        if (commonResponse.getData() != null && !commonResponse.getData().toStringUtf8().equals("null")) {
            List<MetricNode> resultData = JsonUtil.json2List(commonResponse.getData().toStringUtf8(), new TypeReference<List<MetricNode>>() {
            });
            if (resultData != null) {
                metricNodes = resultData;
            }
        }
        metricNodes = metricNodes.stream()
                .sorted(((o1, o2) -> o1.getTimestamp() == o2.getTimestamp() ? 0 : ((o1.getTimestamp() - o2.getTimestamp()) > 0 ? 1 : -1)))
                .collect(Collectors.toList());

        Map<String, Object> dataMap = Maps.newHashMap();
        if (metricNodes != null) {
            metricNodes.forEach(metricNode -> {
                List<MetricNode> nodes = (List<MetricNode>) dataMap.get(metricNode.getResource());
                if (nodes == null) {
                    nodes = Lists.newArrayList();
                }
                nodes.add(metricNode);
                dataMap.put(metricNode.getResource(), nodes);
            });
        }

        return ReturnResult.build(StatusCode.SUCCESS, Dict.SUCCESS, dataMap);
    }

    @GetMapping("monitor/checkHealth")
    public ReturnResult checkHealthService() {

        if (healthRecord == null) {
            return selfCheckService();
        }
        return ReturnResult.build(StatusCode.SUCCESS, Dict.SUCCESS, healthRecord);
    }

    @GetMapping("monitor/selfCheck")
    public ReturnResult selfCheckService() {
        Map<String,Object> newHealthRecord = new ConcurrentHashMap<>();
        Map<String,Map> componentMap = new ConcurrentHashMap<>();
        componentMap.put("proxy",new ConcurrentHashMap());
        componentMap.put("serving",new ConcurrentHashMap());
        Map<String,List<String>> addressMap = componentService.getComponentAddresses();
        componentService.pullService();
        List<ComponentService.ServiceInfo> serviceInfos = componentService.getServiceInfos();

        List batchList = new ArrayList<>();
        for(String component: addressMap.keySet()) {
            if (component.equals("admin")) {
                continue;
            }
            for (String address : addressMap.get(component)) {
                batchList.add(new java.lang.Object());
            }
        }

        for (ComponentService.ServiceInfo serviceInfo: serviceInfos) {
            batchList.add(new java.lang.Object());
        }

        final CountDownLatch countDownLatch = new CountDownLatch(batchList.size());
        for(String component: addressMap.keySet()) {
            if (component.equals("admin")) {
                continue;
            }
            for (String address : addressMap.get(component)) {
                executor.submit(() -> {
                    try {
                        checkRemoteHealth(componentMap, address, component);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    countDownLatch.countDown();
                });
            }
        }
        for (ComponentService.ServiceInfo serviceInfo: serviceInfos) {
            executor.submit(() -> {
                try {
                    checkInferenceService(componentMap,serviceInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                countDownLatch.countDown();
            });
        }
        try {
            countDownLatch.await(10,TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        newHealthRecord.put("timeStamp", System.currentTimeMillis());
        newHealthRecord.put("info", componentMap);
        healthRecord = newHealthRecord;
        return ReturnResult.build(StatusCode.SUCCESS, Dict.SUCCESS, healthRecord);
    }

    private CommonServiceGrpc.CommonServiceBlockingStub getMonitorServiceBlockStub(String host, int port) {
        Preconditions.checkArgument(StringUtils.isNotBlank(host), "parameter host is blank");
        Preconditions.checkArgument(port != 0, "parameter port was wrong");

        if (!NetUtils.isValidAddress(host + ":" + port)) {
            throw new SysException("invalid address");
        }

        if (!componentService.isAllowAccess(host, port)) {
            throw new RemoteRpcException("no allow access, target: " + host + ":" + port);
        }

        ManagedChannel managedChannel = grpcConnectionPool.getManagedChannel(host, port);
        CommonServiceGrpc.CommonServiceBlockingStub blockingStub = CommonServiceGrpc.newBlockingStub(managedChannel);
        return blockingStub;
    }

    private InferenceServiceGrpc.InferenceServiceBlockingStub getInferenceServiceBlockingStub(String host, int port, int timeout) throws Exception {
        Preconditions.checkArgument(StringUtils.isNotBlank(host), "host is blank");
        if (!NetUtils.isValidAddress(host + ":" + port)) {
            throw new SysException("invalid address");
        }

        ManagedChannel managedChannel = grpcConnectionPool.getManagedChannel(host, port);
        InferenceServiceGrpc.InferenceServiceBlockingStub blockingStub = InferenceServiceGrpc.newBlockingStub(managedChannel);
        return blockingStub.withDeadlineAfter(timeout, TimeUnit.MILLISECONDS);
    }


    private void checkRemoteHealth(Map<String,Map> componentMap, String address, String component) {
            Map<String,List> currentComponentMap = componentMap.get(component);
            String host = address.substring(0,address.indexOf(":"));
            int port = Integer.parseInt(address.substring(address.indexOf(":") + 1));
            if (!currentComponentMap.containsKey(address)) {
                currentComponentMap.put(address, new Vector<>());
            }
            List currentList = currentComponentMap.get(address);
            CommonServiceGrpc.CommonServiceBlockingStub blockingStub = getMonitorServiceBlockStub(host, port);
            CommonServiceProto.HealthCheckRequest.Builder builder = CommonServiceProto.HealthCheckRequest.newBuilder();
            builder.setMode("Test");
            builder.setVersion("0.1");
            builder.setType("Test");
            CommonServiceProto.CommonResponse commonResponse = blockingStub.checkHealthService(builder.build());

            Map<String,Object> currentInfoMap = new HashMap<>();
            currentInfoMap.put("type","statusInfo");
            if (commonResponse.getData() == null || commonResponse.getData().toStringUtf8() == "null") {
                currentInfoMap.put("data",null);
            }
            else{
                currentInfoMap.put("data",JsonUtil.json2Object(commonResponse.getData().toStringUtf8(),Map.class));
            }
            currentList.add(currentInfoMap);
        return;
    }

    private void checkInferenceService(Map<String,Map> componentMap, ComponentService.ServiceInfo serviceInfo) throws Exception{
        InferenceServiceProto.InferenceMessage.Builder inferenceMessageBuilder = InferenceServiceProto.InferenceMessage.newBuilder();
        ClassPathResource inferenceTest;
        if (serviceInfo.getName().equals(Dict.SERVICENAME_BATCH_INFERENCE)) {
            inferenceTest = new ClassPathResource("batchInferenceTest.json");
        } else {
            inferenceTest = new ClassPathResource("inferenceTest.json");
        }
        try {
            File jsonFile = inferenceTest.getFile();
            ObjectMapper objectMapper = new ObjectMapper();
            Map map = objectMapper.readValue(jsonFile, Map.class);
            map.put("serviceId", serviceInfo.getServiceId());
            String host = serviceInfo.getHost();
            int port = serviceInfo.getPort();
            inferenceMessageBuilder.setBody(ByteString.copyFrom(JsonUtil.object2Json(map), "UTF-8"));
            InferenceServiceProto.InferenceMessage inferenceMessage = inferenceMessageBuilder.build();
            InferenceServiceGrpc.InferenceServiceBlockingStub blockingStub =
                    this.getInferenceServiceBlockingStub(host, port, 3000);
            InferenceServiceProto.InferenceMessage result;
            if (serviceInfo.getName().equals(Dict.SERVICENAME_BATCH_INFERENCE)) {
                result = blockingStub.batchInference(inferenceMessage);
            }
            else {
                result = blockingStub.inference(inferenceMessage);
            }
            Map<String,List> currentComponentMap = componentMap.get("serving");
            if (!currentComponentMap.containsKey(serviceInfo.getHost() + ":" + port)) {
                currentComponentMap.put(serviceInfo.getHost() + ":" + port, new Vector<>());
            }
            List currentList = currentComponentMap.get(serviceInfo.getHost() + ":" + port);
            Map<String,Object> currentInfoMap = new HashMap<>();
            currentInfoMap.put("type","inference");
            if (result.getBody() == null || result.getBody().toStringUtf8() == "null") {
                currentInfoMap.put("data",null);
            }
            else{
                Map resultMap = JsonUtil.json2Object(result.getBody().toStringUtf8(),Map.class);
                Object returnCode = resultMap.get("retcode");
                Object returnMessage = resultMap.get("retmsg");
                resultMap.clear();
                resultMap.put("serviceId",serviceInfo.getServiceId());
                resultMap.put("retcode",returnCode);
                resultMap.put("retmsg",returnMessage);
                currentInfoMap.put("data",resultMap);
                currentList.add(currentInfoMap);
            }
        } catch (IOException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }
    @Scheduled(cron = "5/10 * * * * ? ")
    private void scheduledCheck() throws InterruptedException {
        if (this.healthRecord != null && System.currentTimeMillis() - Long.valueOf(this.healthRecord.get("timeStamp").toString()) < 150000) {
            return;
        }
        Map<String,Object> newHealthRecord = new ConcurrentHashMap<>();
        Map<String,Map> componentMap = new ConcurrentHashMap<>();
        Map<String,List<String>> addressMap = componentService.getComponentAddresses();
        componentService.pullService();
        List<ComponentService.ServiceInfo> serviceInfos = componentService.getServiceInfos();
        componentMap.put("proxy",new ConcurrentHashMap());
        componentMap.put("serving",new ConcurrentHashMap());
        List batchList = new ArrayList<>();
        for(String component: addressMap.keySet()) {
            if (component.equals("admin")) {
                continue;
            }
            for (String address : addressMap.get(component)) {
                batchList.add(new java.lang.Object());
            }
        }

        for (ComponentService.ServiceInfo serviceInfo: serviceInfos) {
            batchList.add(new java.lang.Object());
        }

        final CountDownLatch countDownLatch = new CountDownLatch(batchList.size());
        for(String component: addressMap.keySet()) {
            if (component.equals("admin")) {
                continue;
            }
            for (String address : addressMap.get(component)) {
                executor.submit(() -> {
                    try {
                        checkRemoteHealth(componentMap, address, component);
                        countDownLatch.countDown();
                    } catch (Exception e) {
                        countDownLatch.countDown();
                    }
                });
            }
        }
        for (ComponentService.ServiceInfo serviceInfo: serviceInfos) {
            executor.submit(() -> {
                try {
                    checkInferenceService(componentMap,serviceInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                countDownLatch.countDown();
            });
        }
        countDownLatch.await(10,TimeUnit.SECONDS);
        newHealthRecord.put("timeStamp", System.currentTimeMillis());
        newHealthRecord.put("info", componentMap);
        healthRecord = newHealthRecord;
    }
}
