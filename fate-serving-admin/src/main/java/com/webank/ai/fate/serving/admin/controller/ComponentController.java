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

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.webank.ai.fate.api.networking.common.CommonServiceGrpc;
import com.webank.ai.fate.api.networking.common.CommonServiceProto;
import com.webank.ai.fate.serving.admin.bean.ServiceConfiguration;
import com.webank.ai.fate.serving.admin.services.ComponentService;
import com.webank.ai.fate.serving.admin.utils.NetAddressChecker;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.exceptions.RemoteRpcException;
import com.webank.ai.fate.serving.core.exceptions.SysException;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import com.webank.ai.fate.serving.core.utils.NetUtils;
import io.grpc.ManagedChannel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description Service management
 * @Date: 2020/3/25 11:13
 * @Author: v_dylanxu
 */
@RequestMapping("/api")
@RestController
public class ComponentController {

    private static final Logger logger = LoggerFactory.getLogger(ComponentController.class);

    @Autowired
    ComponentService componentServices;
    GrpcConnectionPool grpcConnectionPool = GrpcConnectionPool.getPool();

    @GetMapping("/component/list")
    public ReturnResult list() {
        ComponentService.NodeData cachedNodeData = componentServices.getCachedNodeData();
        return ReturnResult.build(StatusCode.SUCCESS, Dict.SUCCESS, JsonUtil.json2Object(JsonUtil.object2Json(cachedNodeData), Map.class));
    }

    @GetMapping("/component/listProps")
    public ReturnResult listProps(String host, int port, String keyword) {
        NetAddressChecker.check(host, port);
        ManagedChannel managedChannel = grpcConnectionPool.getManagedChannel(host, port);
        CommonServiceGrpc.CommonServiceBlockingStub blockingStub = CommonServiceGrpc.newBlockingStub(managedChannel);
        blockingStub = blockingStub.withDeadlineAfter(MetaInfo.PROPERTY_GRPC_TIMEOUT, TimeUnit.MILLISECONDS);

        CommonServiceProto.QueryPropsRequest.Builder builder = CommonServiceProto.QueryPropsRequest.newBuilder();
        if (StringUtils.isNotBlank(keyword)) {
            builder.setKeyword(keyword);
        }

        CommonServiceProto.CommonResponse response = blockingStub.listProps(builder.build());
        Map<String, Object> propMap = JsonUtil.json2Object(response.getData().toStringUtf8(), Map.class);

        List<Map> list = propMap.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(entry -> {
                    Map map = Maps.newHashMap();
                    map.put("key", entry.getKey());
                    map.put("value", entry.getValue());
                    return map;
                })
                .collect(Collectors.toList());

        Map data = Maps.newHashMap();
        data.put("total", list.size());
        data.put("rows", list);

        return ReturnResult.build(response.getStatusCode(), response.getMessage(), data);
    }

    @PostMapping("/component/updateConfig")
    public ReturnResult updateConfig(@RequestBody RequestParamWrapper requestParams) {
        String filePath = requestParams.getFilePath();
        String data = requestParams.getData();
        Preconditions.checkArgument(StringUtils.isNotBlank(filePath), "file path is blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(data), "data is blank");

        String host = requestParams.getHost();
        int port = requestParams.getPort();
        NetAddressChecker.check(host, port);

        String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);

        String project = componentServices.getProject(host, port);
        if (project != null && !ServiceConfiguration.isAllowModify(project, fileName)) {
            throw new SysException("the file is not allowed to be modified");
        }

        ManagedChannel managedChannel = grpcConnectionPool.getManagedChannel(host, port);
        CommonServiceGrpc.CommonServiceBlockingStub blockingStub = CommonServiceGrpc.newBlockingStub(managedChannel)
                .withDeadlineAfter(MetaInfo.PROPERTY_GRPC_TIMEOUT, TimeUnit.MILLISECONDS);
        CommonServiceProto.UpdateConfigRequest.Builder builder = CommonServiceProto.UpdateConfigRequest.newBuilder();
        builder.setFilePath(filePath);
        builder.setData(data);

        CommonServiceProto.CommonResponse response = blockingStub.updateConfig(builder.build());
        return ReturnResult.build(response.getStatusCode(), response.getMessage());
    }

}
