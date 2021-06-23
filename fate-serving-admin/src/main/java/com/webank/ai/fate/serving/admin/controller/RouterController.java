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
import com.webank.ai.fate.api.proxy.common.RouterServiceGrpc;
import com.webank.ai.fate.api.proxy.common.RouterServiceProto;
import com.webank.ai.fate.serving.admin.services.ComponentService;
import com.webank.ai.fate.serving.core.bean.GrpcConnectionPool;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import com.webank.ai.fate.serving.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.exceptions.RemoteRpcException;
import com.webank.ai.fate.serving.core.exceptions.SysException;
import com.webank.ai.fate.serving.core.utils.NetUtils;
import io.grpc.ManagedChannel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/router/query")
    public ReturnResult queryModel(String host, int port, RouterServiceProto.RouterTableInfo routerTable, Integer page, Integer pageSize) throws Exception {
        Preconditions.checkArgument(StringUtils.isNotBlank(host), "parameter host is blank");
        Preconditions.checkArgument(port != 0, "parameter port is blank");
        if (page == null || page < 0) {
            page = 1;
        }

        if (pageSize == null) {
            pageSize = 10;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("query router, host: {}, port: {}", host, port);
        }

        RouterServiceGrpc.RouterServiceBlockingStub blockingStub = getRouterServiceBlockingStub(host, port);

        RouterServiceProto.QueryRouterRequest.Builder queryRouterRequestBuilder = RouterServiceProto.QueryRouterRequest.newBuilder();
        if (StringUtils.isBlank(routerTable.getPartyId())) {
            Preconditions.checkArgument(checkPartyId(routerTable.getPartyId()), "parameter partyId must be default or number");
            queryRouterRequestBuilder.setPartyId(routerTable.getPartyId());
        }
        RouterServiceProto.QueryRouterResponse response = blockingStub.queryRouter(queryRouterRequestBuilder.build());
        if (logger.isDebugEnabled()) {
            logger.debug("response: {}", response);
        }

        Map data = Maps.newHashMap();
        List rows = Lists.newArrayList();
        List<RouterServiceProto.RouterTableInfo> routerTableList = response.getRouterTableList();
        int totalSize = 0;
        if (routerTableList != null) {
            totalSize = routerTableList.size();
            routerTableList = routerTableList.stream().sorted(Comparator.comparing(RouterServiceProto.RouterTableInfo::getPartyId)).collect(Collectors.toList());

            // Pagination
            int totalPage = (routerTableList.size() + pageSize - 1) / pageSize;
            if (page <= totalPage) {
                routerTableList = routerTableList.subList((page - 1) * pageSize, Math.min(page * pageSize, routerTableList.size()));
            }

            for (RouterServiceProto.RouterTableInfo routerTableInfo : routerTableList) {
                rows.add(routerTableInfo);
            }
        }

        data.put("total", totalSize);
        data.put("rows", rows);
        return ReturnResult.build(response.getStatusCode(), response.getMessage(), data);
    }

    @GetMapping("/router/add")
    public ReturnResult addRouter(String host, int port, RouterServiceProto.RouterTableInfo routerTable) throws Exception {
        Preconditions.checkArgument(StringUtils.isNotBlank(host), "parameter host is blank");
        Preconditions.checkArgument(port != 0, "parameter port is blank");
        Preconditions.checkArgument(checkPartyId(routerTable.getPartyId()), "parameter partyId must be default or number");
        Preconditions.checkArgument(!NetUtils.isValidAddress(routerTable.getHost() + ":" + routerTable.getPort()), "parameter Network Access format error");
        if (routerTable.getCertficate()) {
            Preconditions.checkArgument(StringUtils.isBlank(routerTable.getCertficatePath()), "parameter CertficatePath is blank");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("add router, host: {}, port: {}", host, port);
        }

        RouterServiceGrpc.RouterServiceBlockingStub blockingStub = getRouterServiceBlockingStub(host, port);

        RouterServiceProto.RouterOperatetRequest.Builder addRouterBuilder = RouterServiceProto.RouterOperatetRequest.newBuilder();
        addRouterBuilder.setRouterTableInfo(routerTable);
        RouterServiceProto.QueryRouterResponse response = blockingStub.addRouter(addRouterBuilder.build());
        if (logger.isDebugEnabled()) {
            logger.debug("response: {}", response);
        }

        if (response == null) {
            throw new RemoteRpcException("Remote rpc error ,target: " + host + ":" + port);
        }
        return ReturnResult.build(response.getStatusCode(), response.getMessage());
    }

    @GetMapping("/router/update")
    public ReturnResult updateRouter(String host, int port, RouterServiceProto.RouterTableInfo routerTable) throws Exception {
        Preconditions.checkArgument(StringUtils.isNotBlank(host), "parameter host is blank");
        Preconditions.checkArgument(port != 0, "parameter port is blank");
        Preconditions.checkArgument(checkPartyId(routerTable.getPartyId()), "parameter partyId must be default or number");
        Preconditions.checkArgument(!NetUtils.isValidAddress(routerTable.getHost() + ":" + routerTable.getPort()), "parameter Network Access format error");
        if (routerTable.getCertficate()) {
            Preconditions.checkArgument(StringUtils.isBlank(routerTable.getCertficatePath()), "parameter CertficatePath is blank");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("add router, host: {}, port: {}", host, port);
        }

        RouterServiceGrpc.RouterServiceBlockingStub blockingStub = getRouterServiceBlockingStub(host, port);

        RouterServiceProto.RouterOperatetRequest.Builder updateRouterBuilder = RouterServiceProto.RouterOperatetRequest.newBuilder();
        updateRouterBuilder.setRouterTableInfo(routerTable);
        RouterServiceProto.QueryRouterResponse response = blockingStub.updateRouter(updateRouterBuilder.build());
        if (logger.isDebugEnabled()) {
            logger.debug("response: {}", response);
        }

        if (response == null) {
            throw new RemoteRpcException("Remote rpc error ,target: " + host + ":" + port);
        }
        return ReturnResult.build(response.getStatusCode(), response.getMessage());
    }

    @GetMapping("/router/delete")
    public ReturnResult deleteRouter(String host, int port, RouterServiceProto.RouterTableInfo routerTable) throws Exception {
        Preconditions.checkArgument(StringUtils.isNotBlank(host), "parameter host is blank");
        Preconditions.checkArgument(port != 0, "parameter port is blank");
        Preconditions.checkArgument(checkPartyId(routerTable.getPartyId()), "parameter partyId must be default or number");
        if (logger.isDebugEnabled()) {
            logger.debug("add router, host: {}, port: {}", host, port);
        }

        RouterServiceGrpc.RouterServiceBlockingStub blockingStub = getRouterServiceBlockingStub(host, port);

        RouterServiceProto.QueryRouterRequest.Builder deleteBuilder = RouterServiceProto.QueryRouterRequest.newBuilder();
        deleteBuilder.setPartyId(routerTable.getPartyId());
        RouterServiceProto.QueryRouterResponse response = blockingStub.deleteRouter(deleteBuilder.build());
        if (logger.isDebugEnabled()) {
            logger.debug("response: {}", response);
        }

        if (response == null) {
            throw new RemoteRpcException("Remote rpc error ,target: " + host + ":" + port);
        }
        return ReturnResult.build(response.getStatusCode(), response.getMessage());
    }


    private RouterServiceGrpc.RouterServiceBlockingStub getRouterServiceBlockingStub(String host, Integer port) throws Exception {
        Preconditions.checkArgument(StringUtils.isNotBlank(host), "parameter host is blank");
        Preconditions.checkArgument(port != null && port.intValue() != 0, "parameter port was wrong");

        if (!NetUtils.isValidAddress(host + ":" + port)) {
            throw new SysException("invalid address");
        }

        if (!componentService.isAllowAccess(host, port)) {
            throw new RemoteRpcException("no allow access, target: " + host + ":" + port);
        }

        ManagedChannel managedChannel = grpcConnectionPool.getManagedChannel(host, port);
        RouterServiceGrpc.RouterServiceBlockingStub blockingStub = RouterServiceGrpc.newBlockingStub(managedChannel);
        blockingStub = blockingStub.withDeadlineAfter(MetaInfo.PROPERTY_GRPC_TIMEOUT, TimeUnit.MILLISECONDS);
        return blockingStub;
    }

    public boolean checkPartyId(String partyId) {
        return "default".equals(partyId) && partyId.matches("^\\d{1,5}$");
    }
}
