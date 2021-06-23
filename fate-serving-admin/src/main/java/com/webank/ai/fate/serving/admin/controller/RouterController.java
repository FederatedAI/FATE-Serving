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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.webank.ai.fate.serving.admin.services.ComponentService;
import com.webank.ai.fate.serving.core.bean.GrpcConnectionPool;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import com.webank.ai.fate.serving.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.exceptions.RemoteRpcException;
import com.webank.ai.fate.serving.core.exceptions.SysException;
import com.webank.ai.fate.serving.core.utils.NetUtils;
import com.webank.ai.fate.serving.proxy.rpc.grpc.RouterTableServiceGrpc;
import com.webank.ai.fate.serving.proxy.rpc.grpc.RouterTableServiceProto;
import io.grpc.ManagedChannel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description Model management
 * @Date: 2020/3/25 11:13
 * @Author: v_dylanxu
 */
@RequestMapping("/api")
@RestController
public class RouterController {

    private static final Logger logger = LoggerFactory.getLogger(RouterController.class);
    GrpcConnectionPool grpcConnectionPool = GrpcConnectionPool.getPool();

    @Autowired
    private ComponentService componentService;

    @PostMapping("/router/query")
    @ResponseBody
    public ReturnResult queryModel(RouterTableServiceProto.RouterTableInfo routerTable, Integer page, Integer pageSize) throws Exception {
        String serverHost = "127.0.0.1";int serverPort=8879;
        Preconditions.checkArgument(StringUtils.isNotBlank(serverHost), "parameter host is blank");
        Preconditions.checkArgument(serverPort != 0, "parameter port is blank");
        if (page == null || page < 0) {
            page = 1;
        }

        if (pageSize == null) {
            pageSize = 10;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("query router, host: {}, port: {}", serverHost, serverPort);
        }

        RouterTableServiceGrpc.RouterTableServiceBlockingStub blockingStub = getRouterTableServiceBlockingStub(serverHost, serverPort);

        RouterTableServiceProto.RouterOperatetRequest.Builder queryRouterRequestBuilder = RouterTableServiceProto.RouterOperatetRequest.newBuilder();
//        if (StringUtils.isNotBlank(routerTable.getPartyId())) {
//            Preconditions.checkArgument(checkPartyId(routerTable.getPartyId()), "parameter partyId must be default or number");
//            queryRouterRequestBuilder.setPartyId(routerTable.getPartyId());
//        }
        RouterTableServiceProto.RouterOperatetResponse response = blockingStub.queryRouter(queryRouterRequestBuilder.build());
        if (logger.isDebugEnabled()) {
            logger.debug("response: {}", response);
        }

        Map data = Maps.newHashMap();
        List rows = Lists.newArrayList();
//        List<RouterTableServiceProto.RouterTableInfo> routerTableList = (List<RouterTableServiceProto.RouterTableInfo>)response.getData().toStringUtf8();
        List<RouterTableServiceProto.RouterTableInfo> routerTableList = new ArrayList<>();
        int totalSize = 0;
        if (routerTableList != null) {
            totalSize = routerTableList.size();
            routerTableList = routerTableList.stream().sorted(Comparator.comparing(RouterTableServiceProto.RouterTableInfo::getPartyId)).collect(Collectors.toList());

            // Pagination
            int totalPage = (routerTableList.size() + pageSize - 1) / pageSize;
            if (page <= totalPage) {
                routerTableList = routerTableList.subList((page - 1) * pageSize, Math.min(page * pageSize, routerTableList.size()));
            }

            for (RouterTableServiceProto.RouterTableInfo routerTableInfo : routerTableList) {
                rows.add(routerTableInfo);
            }
        }

        data.put("total", totalSize);
        data.put("rows", rows);
        return ReturnResult.build(response.getStatusCode(), response.getMessage(), data);
    }

    @PostMapping("/router/add")
    public ReturnResult addRouter(String serverHost, int serverPort, RouterTableServiceProto.RouterTableInfo routerTable) throws Exception {
        Preconditions.checkArgument(StringUtils.isNotBlank(serverHost), "parameter host is blank");
        Preconditions.checkArgument(serverPort != 0, "parameter port is blank");
        Preconditions.checkArgument(checkPartyId(routerTable.getPartyId()), "parameter partyId must be default or number");
        Preconditions.checkArgument(!NetUtils.isValidAddress(routerTable.getHost() + ":" + routerTable.getPort()), "parameter Network Access format error");
        if (routerTable.getUseSSL()) {
            Preconditions.checkArgument(StringUtils.isBlank(routerTable.getCertChainFile()), "parameter CertficatePath is blank");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("add router, host: {}, port: {}", serverHost, serverPort);
        }

        RouterTableServiceGrpc.RouterTableServiceBlockingStub blockingStub = getRouterTableServiceBlockingStub(serverHost, serverPort);

        RouterTableServiceProto.RouterOperatetRequest.Builder addRouterBuilder = RouterTableServiceProto.RouterOperatetRequest.newBuilder();
        addRouterBuilder.setRouterTableInfo(routerTable);
        RouterTableServiceProto.RouterOperatetResponse response = blockingStub.addRouter(addRouterBuilder.build());
        if (logger.isDebugEnabled()) {
            logger.debug("response: {}", response);
        }

        if (response == null) {
            throw new RemoteRpcException("Remote rpc error ,target: " + serverHost + ":" + serverPort);
        }
        return ReturnResult.build(response.getStatusCode(), response.getMessage());
    }

    @PostMapping("/router/update")
    public ReturnResult updateRouter(String serverHost, int serverPort, RouterTableServiceProto.RouterTableInfo routerTable) throws Exception {
        Preconditions.checkArgument(StringUtils.isNotBlank(serverHost), "parameter host is blank");
        Preconditions.checkArgument(serverPort != 0, "parameter port is blank");
        Preconditions.checkArgument(checkPartyId(routerTable.getPartyId()), "parameter partyId must be default or number");
        Preconditions.checkArgument(!NetUtils.isValidAddress(routerTable.getHost() + ":" + routerTable.getPort()), "parameter Network Access format error");
        if (routerTable.getUseSSL()) {
            Preconditions.checkArgument(StringUtils.isBlank(routerTable.getCertChainFile()), "parameter CertficatePath is blank");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("add router, host: {}, port: {}", serverHost, serverPort);
        }

        RouterTableServiceGrpc.RouterTableServiceBlockingStub blockingStub = getRouterTableServiceBlockingStub(serverHost, serverPort);

        RouterTableServiceProto.RouterOperatetRequest.Builder updateRouterBuilder = RouterTableServiceProto.RouterOperatetRequest.newBuilder();
        updateRouterBuilder.setRouterTableInfo(routerTable);
        RouterTableServiceProto.RouterOperatetResponse response = blockingStub.updateRouter(updateRouterBuilder.build());
        if (logger.isDebugEnabled()) {
            logger.debug("response: {}", response);
        }

        if (response == null) {
            throw new RemoteRpcException("Remote rpc error ,target: " + serverHost + ":" + serverPort);
        }
        return ReturnResult.build(response.getStatusCode(), response.getMessage());
    }

    @PostMapping("/router/delete")
    public ReturnResult deleteRouter(String serverHost, int serverPort, RouterTableServiceProto.RouterTableInfo routerTable) throws Exception {
        Preconditions.checkArgument(StringUtils.isNotBlank(serverHost), "parameter host is blank");
        Preconditions.checkArgument(serverPort != 0, "parameter port is blank");
        Preconditions.checkArgument(checkPartyId(routerTable.getPartyId()), "parameter partyId must be default or number");
        if (logger.isDebugEnabled()) {
            logger.debug("add router, host: {}, port: {}", serverHost, serverPort);
        }

        RouterTableServiceGrpc.RouterTableServiceBlockingStub blockingStub = getRouterTableServiceBlockingStub(serverHost, serverPort);

        RouterTableServiceProto.RouterOperatetRequest.Builder deleteBuilder = RouterTableServiceProto.RouterOperatetRequest.newBuilder();
        deleteBuilder.setRouterTableInfo(routerTable);
        RouterTableServiceProto.RouterOperatetResponse response = blockingStub.deleteRouter(deleteBuilder.build());
        if (logger.isDebugEnabled()) {
            logger.debug("response: {}", response);
        }

        if (response == null) {
            throw new RemoteRpcException("Remote rpc error ,target: " + serverHost + ":" + serverPort);
        }
        return ReturnResult.build(response.getStatusCode(), response.getMessage());
    }


    private RouterTableServiceGrpc.RouterTableServiceBlockingStub getRouterTableServiceBlockingStub(String host, Integer port) throws Exception {
        Preconditions.checkArgument(StringUtils.isNotBlank(host), "parameter host is blank");
        Preconditions.checkArgument(port != null && port.intValue() != 0, "parameter port was wrong");

        if (!NetUtils.isValidAddress(host + ":" + port)) {
            throw new SysException("invalid address");
        }

        if (!componentService.isAllowAccess(host, port)) {
            throw new RemoteRpcException("no allow access, target: " + host + ":" + port);
        }

        ManagedChannel managedChannel = grpcConnectionPool.getManagedChannel(host, port);
        RouterTableServiceGrpc.RouterTableServiceBlockingStub blockingStub = RouterTableServiceGrpc.newBlockingStub(managedChannel);
        blockingStub = blockingStub.withDeadlineAfter(MetaInfo.PROPERTY_GRPC_TIMEOUT, TimeUnit.MILLISECONDS);
        return blockingStub;
    }

    public boolean checkPartyId(String partyId) {
        return "default".equals(partyId) && partyId.matches("^\\d{1,5}$");
    }
}
