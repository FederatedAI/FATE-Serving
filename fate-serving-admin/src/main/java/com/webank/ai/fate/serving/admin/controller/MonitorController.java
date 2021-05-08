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
import com.webank.ai.fate.register.zookeeper.ZookeeperRegistry;
import com.webank.ai.fate.serving.admin.services.ComponentService;
import com.webank.ai.fate.serving.common.flow.HealthInfo;
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
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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
            return ReturnResult.build(StatusCode.SYSTEM_ERROR, Dict.FAILED, null);
        }
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


    private void checkRemoteHealth(Map<String,List> componentMap) {
        Map<String,List<String>> addressMap = componentService.getAddressMap();
        if (addressMap == null) {
            return;
        }
        for(String component: addressMap.keySet()) {
            if (component.equals("admin")) {
                continue;
            }
            for(String address: addressMap.get(component)) {
                executor.submit(() -> {
                    String host = address.substring(0,address.indexOf(":"));
                    int port = Integer.parseInt(address.substring(address.indexOf(":") + 1));
                    CommonServiceGrpc.CommonServiceBlockingStub blockingStub = getMonitorServiceBlockStub(host, port);
                    CommonServiceProto.HealthCheckRequest.Builder builder = CommonServiceProto.HealthCheckRequest.newBuilder();
                    builder.setMode("Test");
                    builder.setVersion("0.1");
                    builder.setType("Test");
                    CommonServiceProto.CommonResponse commonResponse = blockingStub.checkHealthService(builder.build());

                    HealthInfo newHealthInfo = new HealthInfo(component,host,port,"machineInfo");
                    if (commonResponse.getData() == null || commonResponse.getData().toStringUtf8() == "null") {
                        newHealthInfo.setData(null);
                    }
                    else{
                        newHealthInfo.setData(commonResponse.getData().toStringUtf8());
                    }
                    if (!componentMap.containsKey(component)) {
                        componentMap.put(component,new Vector());
                    }
                    componentMap.get(component).add(newHealthInfo);
                });
            }
        }
    }

    private void checkInferenceService(Map<String,List> componentMap) throws Exception{
        componentService.pullService();
        List<ComponentService.ServiceInfo> serviceInfos = componentService.getServiceInfos();
        for (ComponentService.ServiceInfo serviceInfo: serviceInfos) {
            executor.submit(() -> {
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
                    HealthInfo newHealthInfo = new HealthInfo("serving",host, port,"inference");
                    if (result.getBody() == null || result.getBody().toStringUtf8() == "null") {
                        newHealthInfo.setData(null);
                    }
                    else{
                        Map resultMap = JsonUtil.json2Object(result.getBody().toStringUtf8(),Map.class);
                        Object returnCode = resultMap.get("retcode");
                        Object returnMessage = resultMap.get("retmsg");
                        resultMap.clear();
                        resultMap.put("retcode",returnCode);
                        resultMap.put("retmsg",returnMessage);
                        newHealthInfo.setData(resultMap);
                    }
                    if (!componentMap.containsKey("serving")) {
                        componentMap.put("serving",new Vector());
                    }
                    componentMap.get("serving").add(newHealthInfo);
                } catch (IOException e) {
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
    @Scheduled(fixedRate = 10000)
    private void selfCheck() {
        Map<String,Object> newHealthRecord = new ConcurrentHashMap<>();
        Map<String,List> componentMap = new ConcurrentHashMap<>();
        checkRemoteHealth(componentMap);
        try {
            checkInferenceService(componentMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        newHealthRecord.put("timeStamp", System.currentTimeMillis());
        newHealthRecord.put("info", componentMap);
        this.healthRecord = newHealthRecord;
    }
}
