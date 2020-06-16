package com.webank.ai.fate.serving.admin.controller;


import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.webank.ai.fate.api.networking.common.CommonServiceGrpc;
import com.webank.ai.fate.api.networking.common.CommonServiceProto;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.GrpcConnectionPool;
import com.webank.ai.fate.serving.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.flow.JvmInfo;
import com.webank.ai.fate.serving.core.flow.MetricNode;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import io.grpc.ManagedChannel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@RequestMapping("/api")
@RestController
public class MonitorController {

    GrpcConnectionPool grpcConnectionPool = GrpcConnectionPool.getPool();

    @Value("${grpc.timeout:5000}")
    private int timeout;

    @GetMapping("/monitor/queryJvm")
    public ReturnResult queryJvmData(String host, int port) {
        CommonServiceGrpc.CommonServiceBlockingStub blockingStub = getMonitorServiceBlockStub(host, port);
        blockingStub = blockingStub.withDeadlineAfter(timeout, TimeUnit.MILLISECONDS);
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
        blockingStub = blockingStub.withDeadlineAfter(timeout, TimeUnit.MILLISECONDS);
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
        blockingStub = blockingStub.withDeadlineAfter(timeout, TimeUnit.MILLISECONDS);
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

    private CommonServiceGrpc.CommonServiceBlockingStub getMonitorServiceBlockStub(String host, int port) {
        Preconditions.checkArgument(StringUtils.isNotBlank(host), "parameter host is blank");
        Preconditions.checkArgument(port != 0, "parameter port was wrong");
        ManagedChannel managedChannel = grpcConnectionPool.getManagedChannel(host, port);
        CommonServiceGrpc.CommonServiceBlockingStub blockingStub = CommonServiceGrpc.newBlockingStub(managedChannel);
        return blockingStub;
    }

}
