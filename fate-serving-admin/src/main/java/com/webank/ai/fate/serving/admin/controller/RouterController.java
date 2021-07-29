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
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.webank.ai.fate.serving.admin.services.ComponentService;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.exceptions.RemoteRpcException;
import com.webank.ai.fate.serving.core.exceptions.SysException;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import com.webank.ai.fate.serving.core.utils.NetUtils;
import com.webank.ai.fate.serving.proxy.rpc.grpc.RouterTableServiceGrpc;
import com.webank.ai.fate.serving.proxy.rpc.grpc.RouterTableServiceProto;
import io.grpc.ManagedChannel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RequestMapping("/api")
@RestController
public class RouterController {

    private static final Logger logger = LoggerFactory.getLogger(RouterController.class);
    GrpcConnectionPool grpcConnectionPool = GrpcConnectionPool.getPool();

    @Autowired
    ComponentService componentService;

    @PostMapping("/router/query")
    public ReturnResult queryModel(@RequestBody RouterTableRequest routerTable) {
        logger.info("pppppppppppppppppp");
        String serverHost = routerTable.getServerHost();
        Integer serverPort = routerTable.getServerPort();
        Integer page = routerTable.getPage();
        Integer pageSize = routerTable.getPageSize();
        checkAddress(serverHost, serverPort);
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
        RouterTableServiceProto.RouterOperatetResponse response = blockingStub.queryRouter(queryRouterRequestBuilder.build());
//        if (logger.isDebugEnabled()) {
//            logger.debug("response: {}", response);
//        }
        logger.info("response: {}", response);
        Map<String, Object> data = Maps.newHashMap();
        List<RouterTableResponseRecord> rows = Lists.newArrayList();
        List<RouterTableResponseRecord> routerTableList =
                JsonUtil.json2List(response.getData().toStringUtf8(), new TypeReference<List<RouterTableResponseRecord>>() {
                });
        int totalSize = 0;
        if (routerTableList != null) {
            String filterPartyId = (routerTable.getRouterTableList() == null || routerTable.getRouterTableList().size() == 0)
                    ? null : routerTable.getRouterTableList().get(0).getPartyId();
            routerTableList = routerTableList.stream()
                    .filter(record -> record.getPartyId().equals(filterPartyId) || StringUtils.isBlank(filterPartyId))
                    .sorted((prev, next) -> comparePartyId(prev.getPartyId(),next.getPartyId()))
                    .collect(Collectors.toList());
            totalSize = routerTableList.size();

            // Pagination
            int totalPage = (routerTableList.size() + pageSize - 1) / pageSize;
            if (page <= totalPage) {
                routerTableList = routerTableList.subList((page - 1) * pageSize, Math.min(page * pageSize, routerTableList.size()));
            }
            rows.addAll(routerTableList);
        }

        data.put("total", totalSize);
        data.put("rows", rows);
        return ReturnResult.build(response.getStatusCode(), response.getMessage(), data);
    }

    private RouterTableServiceGrpc.RouterTableServiceBlockingStub getRouterTableServiceBlockingStub(String host, Integer port) {
        Preconditions.checkArgument(StringUtils.isNotBlank(host), "parameter host is blank");
        Preconditions.checkArgument(port != null && port != 0, "parameter port was wrong");
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

    @PostMapping("/router/add")
    public ReturnResult addRouter(@RequestBody RouterTableRequest routerTables) {
        checkAddress(routerTables.getServerHost(), routerTables.getServerPort());
        for (RouterTableServiceProto.RouterTableInfo routerTable : parseRouterInfo(routerTables.getRouterTableList())) {
            checkParameter(routerTable);
        }
        RouterTableServiceGrpc.RouterTableServiceBlockingStub blockingStub = getRouterTableServiceBlockingStub(routerTables.getServerHost(), routerTables.getServerPort());
        RouterTableServiceProto.RouterOperatetRequest.Builder addRouterBuilder = RouterTableServiceProto.RouterOperatetRequest.newBuilder();
        addRouterBuilder.addAllRouterTableInfo(parseRouterInfo(routerTables.getRouterTableList()));
        RouterTableServiceProto.RouterOperatetResponse response = blockingStub.addRouter(addRouterBuilder.build());
        if (logger.isDebugEnabled()) {
            logger.debug("response: {}", response);
        }

        if (response == null) {
            throw new RemoteRpcException("Remote rpc error ,target: " + routerTables.getServerHost() + ":" + routerTables.getServerPort());
        }
        return ReturnResult.build(response.getStatusCode(), response.getMessage());
    }

    @PostMapping("/router/update")
    public ReturnResult updateRouter(@RequestBody RouterTableRequest routerTables) {
        checkAddress(routerTables.getServerHost(), routerTables.getServerPort());
        for (RouterTableRequest.RouterTable routerTable : routerTables.getRouterTableList()) {
            Preconditions.checkArgument(checkPartyId(routerTable.getPartyId()), "parameter partyId : {" + routerTable.getPartyId() + "} must be default or number");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("add router, host: {}, port: {}", routerTables.getServerHost(), routerTables.getServerPort());
        }
        RouterTableServiceGrpc.RouterTableServiceBlockingStub blockingStub = getRouterTableServiceBlockingStub(routerTables.getServerHost(), routerTables.getServerPort());
        RouterTableServiceProto.RouterOperatetRequest.Builder addRouterBuilder = RouterTableServiceProto.RouterOperatetRequest.newBuilder();
        addRouterBuilder.addAllRouterTableInfo(parseRouterInfo(routerTables.getRouterTableList()));
        RouterTableServiceProto.RouterOperatetResponse response = blockingStub.updateRouter(addRouterBuilder.build());
        if (logger.isDebugEnabled()) {
            logger.debug("response: {}", response);
        }

        if (response == null) {
            throw new RemoteRpcException("Remote rpc error ,target: " + routerTables.getServerHost() + ":" + routerTables.getServerPort());
        }
        return ReturnResult.build(response.getStatusCode(), response.getMessage());
    }

    @PostMapping("/router/delete")
    public ReturnResult deleteRouter(@RequestBody RouterTableRequest routerTables) {
        checkAddress(routerTables.getServerHost(), routerTables.getServerPort());
        for (RouterTableRequest.RouterTable routerTable : routerTables.getRouterTableList()) {
            Preconditions.checkArgument(checkPartyId(routerTable.getPartyId()), "parameter partyId : {" + routerTable.getPartyId() + "} must be default or number");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("add router, host: {}, port: {}", routerTables.getServerHost(), routerTables.getServerPort());
        }
        RouterTableServiceGrpc.RouterTableServiceBlockingStub blockingStub = getRouterTableServiceBlockingStub(routerTables.getServerHost(), routerTables.getServerPort());
        RouterTableServiceProto.RouterOperatetRequest.Builder addRouterBuilder = RouterTableServiceProto.RouterOperatetRequest.newBuilder();
        addRouterBuilder.addAllRouterTableInfo(parseRouterInfo(routerTables.getRouterTableList()));
        RouterTableServiceProto.RouterOperatetResponse response = blockingStub.deleteRouter(addRouterBuilder.build());
        if (logger.isDebugEnabled()) {
            logger.debug("response: {}", response);
        }

        if (response == null) {
            throw new RemoteRpcException("Remote rpc error ,target: " + routerTables.getServerHost() + ":" + routerTables.getServerPort());
        }
        return ReturnResult.build(response.getStatusCode(), response.getMessage());
    }

    private void checkParameter(RouterTableServiceProto.RouterTableInfo routerTable) {
        Preconditions.checkArgument(checkPartyId(routerTable.getPartyId()), "parameter partyId:{" + routerTable.getPartyId() + "} must be default or number");
        Preconditions.checkArgument(NetUtils.isValidAddress(routerTable.getHost() + ":" + routerTable.getPort()), "parameter Network Access : {" + routerTable.getHost() + ":" + routerTable.getPort() + "} format error");
        Preconditions.checkArgument(StringUtils.isNotBlank(routerTable.getServerType()), "parameter serverType must is blank");
        if (routerTable.getUseSSL()) {
            Preconditions.checkArgument(StringUtils.isBlank(routerTable.getCertChainFile()), "parameter certificatePath is blank");
        }
    }

    private void checkAddress(String serverHost, Integer serverPort) {
        Preconditions.checkArgument(StringUtils.isNotBlank(serverHost), "parameter serverHost is blank");
        Preconditions.checkArgument(serverPort != 0, "parameter serverPort is blank");
        if (logger.isDebugEnabled()) {
            logger.debug("add router, host: {}, port: {}", serverHost, serverPort);
        }
    }

    public boolean checkPartyId(String partyId) {
        return "default".equals(partyId) || partyId.matches("^\\d{1,5}$");
    }

    public List<RouterTableServiceProto.RouterTableInfo> parseRouterInfo(List<RouterTableRequest.RouterTable> routerTableList) {
        List<RouterTableServiceProto.RouterTableInfo> routerTableInfoList = new ArrayList<>();
        if (routerTableList == null) {
            return routerTableInfoList;
        }
        for (RouterTableRequest.RouterTable routerTable : routerTableList) {
            if (routerTable == null) continue;
            RouterTableServiceProto.RouterTableInfo.Builder builder = RouterTableServiceProto.RouterTableInfo.newBuilder();
            builder.setPartyId(routerTable.getPartyId())
                    .setHost(routerTable.getIp())
                    .setPort(routerTable.getPort())
                    .setUseSSL(routerTable.isUseSSL())
                    .setNegotiationType(routerTable.getNegotiationType())
                    .setCertChainFile(routerTable.getCertChainFile())
                    .setPrivateKeyFile(routerTable.getPrivateKeyFile())
                    .setCaFile(routerTable.getCaFile())
                    .setServerType(routerTable.getServerType());
            routerTableInfoList.add(builder.build());
        }
        return routerTableInfoList;
    }

    public static int comparePartyId(String prev , String next){
//        prev = "default".equals(prev)?"0": prev;
//        next = "default".equals(next)?"0": next;
//        return prev.compareTo(next);
        String numberReg = "^\\d{1,9}$";
        Integer prevInt = prev.matches(numberReg)?Integer.parseInt(prev): 0;
        Integer nextInt = next.matches(numberReg)?Integer.parseInt(next): 0;
        return prevInt - nextInt;
    }
}
