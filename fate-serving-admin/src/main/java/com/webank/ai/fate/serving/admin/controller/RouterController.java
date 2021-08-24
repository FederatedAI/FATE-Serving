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

import com.google.common.collect.Maps;
import com.webank.ai.fate.api.networking.common.CommonServiceGrpc;
import com.webank.ai.fate.api.networking.common.CommonServiceProto;
import com.webank.ai.fate.register.url.URL;
import com.webank.ai.fate.register.zookeeper.ZookeeperRegistry;
import com.webank.ai.fate.serving.admin.services.ComponentService;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.exceptions.RemoteRpcException;
import com.webank.ai.fate.serving.core.exceptions.SysException;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import com.webank.ai.fate.serving.core.utils.NetUtils;
import com.webank.ai.fate.serving.core.utils.ParameterUtils;
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

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RequestMapping("/api")
@RestController
public class RouterController {

    private static final Logger logger = LoggerFactory.getLogger(RouterController.class);
    GrpcConnectionPool grpcConnectionPool = GrpcConnectionPool.getPool();
    @Autowired
    ZookeeperRegistry zookeeperRegistry;
    @Autowired
    ComponentService componentService;
    String ROUTER_URL = "proxy/online/queryRouter";


    @PostMapping("/router/query")
    public ReturnResult queryRouter(@RequestBody RouterTableRequest routerTable) {

        List<URL> urls = zookeeperRegistry.getCacheUrls(URL.valueOf(ROUTER_URL));
        if (urls == null || urls.size() == 0) {
            return new ReturnResult();
        }
        Map<String, Object> data = Maps.newHashMap();
        String serverHost = routerTable.getServerHost();
        Integer serverPort = routerTable.getServerPort();
        checkAddress(serverHost, serverPort);
        String routerTableInfo = "";
        boolean matched = false;
        for (URL url : urls) {
            logger.info("url {} {} {} {}", url.getHost(), url.getPort(), serverHost, serverPort);
            if (serverHost.equals(url.getHost()) && serverPort.intValue() == serverPort) { // todo-wcy
                matched = true;
            }
        }
        int statusCode;
        String retMsg;

        if (!matched) {
            ManagedChannel managedChannel = grpcConnectionPool.getManagedChannel(serverHost, serverPort);
            CommonServiceGrpc.CommonServiceBlockingStub blockingStub = CommonServiceGrpc.newBlockingStub(managedChannel);
            blockingStub = blockingStub.withDeadlineAfter(MetaInfo.PROPERTY_GRPC_TIMEOUT, TimeUnit.MILLISECONDS);
            CommonServiceProto.QueryPropsRequest.Builder builder = CommonServiceProto.QueryPropsRequest.newBuilder();
            CommonServiceProto.CommonResponse response = blockingStub.listProps(builder.build());
            Map<String, Object> propMap = JsonUtil.json2Object(response.getData().toStringUtf8(), Map.class);
            routerTableInfo = propMap.get(Dict.PROXY_ROUTER_TABLE) != null ? propMap.get(Dict.PROXY_ROUTER_TABLE).toString() : "";
            statusCode = response.getStatusCode();
            retMsg = response.getMessage();
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("query router, host: {}, port: {}", serverHost, serverPort);
            }
            RouterTableServiceGrpc.RouterTableServiceBlockingStub blockingStub = getRouterTableServiceBlockingStub(serverHost, serverPort);
            RouterTableServiceProto.RouterOperatetRequest.Builder queryRouterRequestBuilder = RouterTableServiceProto.RouterOperatetRequest.newBuilder();
            RouterTableServiceProto.RouterOperatetResponse response = blockingStub.queryRouter(queryRouterRequestBuilder.build());
            routerTableInfo = response.getData().toStringUtf8();
            statusCode = response.getStatusCode();
            retMsg = response.getMessage();
        }
        data.put("routerTable", routerTableInfo);
        data.put("changeAble", matched);
        return ReturnResult.build(statusCode, retMsg, data);
    }

    private RouterTableServiceGrpc.RouterTableServiceBlockingStub getRouterTableServiceBlockingStub(String host, Integer port) {
        ParameterUtils.checkArgument(StringUtils.isNotBlank(host), "parameter host is blank");
        ParameterUtils.checkArgument(port != null && port != 0, "parameter port was wrong");
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


    @PostMapping("/router/save")
    public ReturnResult saveRouter(@RequestBody RouterTableRequest routerTables) {

        logger.info("save router table {}", routerTables.getRouterTableList());


        checkAddress(routerTables.getServerHost(), routerTables.getServerPort());

        String content = JsonUtil.object2Json(routerTables.getRouterTableList());
//        for (Map routerTable : parseRouterInfo(routerTables.getRouterTableList())) {
//            checkParameter(routerTable);
//        }
        RouterTableServiceGrpc.RouterTableServiceBlockingStub blockingStub = getRouterTableServiceBlockingStub(routerTables.getServerHost(), routerTables.getServerPort());
        RouterTableServiceProto.RouterOperatetRequest.Builder addRouterBuilder = RouterTableServiceProto.RouterOperatetRequest.newBuilder();
        addRouterBuilder.setRouterInfo(content);
        RouterTableServiceProto.RouterOperatetResponse response = blockingStub.saveRouter(addRouterBuilder.build());
        if (logger.isDebugEnabled()) {
            logger.debug("response: {}", response);
        }




        if (response == null) {
            throw new RemoteRpcException("Remote rpc error ,target: " + routerTables.getServerHost() + ":" + routerTables.getServerPort());
        }

        return ReturnResult.build(response.getStatusCode(), response.getMessage());
    }

//    private void checkParameter(RouterTableServiceProto.RouterTableInfo routerTable) {
//        ParameterUtils.checkArgument(checkPartyId(routerTable.getPartyId()), "parameter partyId:{" + routerTable.getPartyId() + "} must be default or number");
//        ParameterUtils.checkArgument(NetUtils.isValidAddress(routerTable.getHost() + ":" + routerTable.getPort()), "parameter Network Access : {" + routerTable.getHost() + ":" + routerTable.getPort() + "} format error");
//        ParameterUtils.checkArgument(StringUtils.isNotBlank(routerTable.getServerType()), "parameter serverType must is blank");
//        if (routerTable.getUseSSL()) {
//            ParameterUtils.checkArgument(StringUtils.isBlank(routerTable.getCertChainFile()), "parameter certificatePath is blank");
//        }
//    }

    private void checkAddress(String serverHost, Integer serverPort) {
        ParameterUtils.checkArgument(StringUtils.isNotBlank(serverHost), "parameter serverHost is blank");
        ParameterUtils.checkArgument(serverPort != 0, "parameter serverPort is blank");

        if (logger.isDebugEnabled()) {
            logger.debug("add router, host: {}, port: {}", serverHost, serverPort);
        }
    }

//    private void checkAddress(RouterTableRequest routerTables) {
//        ParameterUtils.checkArgument(StringUtils.isNotBlank(routerTables.getServerHost()), "parameter serverHost is blank");
//        ParameterUtils.checkArgument(routerTables.getServerPort() != 0, "parameter serverPort is blank");
//
//        List<RouterTableRequest.RouterTable> routerTableList = routerTables.getRouterTableList();
//        if(routerTableList != null){
//            for (RouterTableRequest.RouterTable routerTable : routerTableList) {
//                ParameterUtils.checkArgument(NetUtils.isValidAddress(routerTable.getIp() + ":" + routerTable.getPort()), "parameter Network Access : {" + routerTable.getIp() + ":" + routerTable.getPort() + "} format error");
//            }
//        }
//    }

    public boolean checkPartyId(String partyId) {
        return "default".equals(partyId) || partyId.matches("^\\d{1,5}$");
    }

//    public List<RouterTableServiceProto.RouterTableInfo> parseRouterInfo(List<RouterTableRequest.RouterTable> routerTableList) {
//        List<RouterTableServiceProto.RouterTableInfo> routerTableInfoList = new ArrayList<>();
//        if (routerTableList == null) {
//            return routerTableInfoList;
//        }
//        for (RouterTableRequest.RouterTable routerTable : routerTableList) {
//            if (routerTable == null) continue;
//            RouterTableServiceProto.RouterTableInfo.Builder builder = RouterTableServiceProto.RouterTableInfo.newBuilder();
//            builder.setPartyId(routerTable.getPartyId())
//                    .setHost(routerTable.getIp())
//                    .setPort(routerTable.getPort())
//                    .setUseSSL(routerTable.isUseSSL())
//                    .setNegotiationType(routerTable.getNegotiationType())
//                    .setCertChainFile(routerTable.getCertChainFile())
//                    .setPrivateKeyFile(routerTable.getPrivateKeyFile())
//                    .setCaFile(routerTable.getCaFile())
//                    .setServerType(routerTable.getServerType());
//            routerTableInfoList.add(builder.build());
//        }
//        return routerTableInfoList;
//    }

    public static int comparePartyId(String prev, String next) {
        String numberReg = "^\\d{1,9}$";
        Integer prevInt = prev.matches(numberReg) ? Integer.parseInt(prev) : 0;
        Integer nextInt = next.matches(numberReg) ? Integer.parseInt(next) : 0;
        return prevInt - nextInt;
    }
}
