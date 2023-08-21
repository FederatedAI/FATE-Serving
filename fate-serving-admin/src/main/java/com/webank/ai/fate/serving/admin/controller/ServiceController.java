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
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ListenableFuture;
import com.webank.ai.fate.api.networking.common.CommonServiceGrpc;
import com.webank.ai.fate.api.networking.common.CommonServiceProto;
import com.webank.ai.fate.register.common.Constants;
import com.webank.ai.fate.register.url.URL;
import com.webank.ai.fate.register.zookeeper.ZookeeperRegistry;
import com.webank.ai.fate.serving.admin.bean.VerifyService;
import com.webank.ai.fate.serving.admin.services.ComponentService;
import com.webank.ai.fate.serving.admin.utils.NetAddressChecker;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.exceptions.RemoteRpcException;
import com.webank.ai.fate.serving.core.exceptions.SysException;
import com.webank.ai.fate.serving.core.utils.NetUtils;
import io.grpc.ManagedChannel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description Service management
 * @Date: 2020/3/25 11:13
 * @Author: v_dylanxu
 */
@RequestMapping("/api")
@RestController
public class ServiceController {

    private static final Logger logger = LoggerFactory.getLogger(ServiceController.class);

    @Autowired
    private ZookeeperRegistry zookeeperRegistry;
    @Autowired
    ComponentService componentService;

    GrpcConnectionPool grpcConnectionPool = GrpcConnectionPool.getPool();


    Set<String>  filterSet = Sets.newHashSet("batchInference","inference","unaryCall");

    /**
     * 列出集群中所注册的所有接口
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/service/list")
    public ReturnResult listRegistered(Integer page, Integer pageSize) {
        int defaultPage = 1;
        int defaultPageSize = 10;

        if (page == null || page <= 0) {
            page = defaultPage;
        }

        if (pageSize == null || pageSize <= 0) {
            pageSize = defaultPageSize;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("try to query all registered service");
        }
        Properties properties = zookeeperRegistry.getCacheProperties();


        List<ServiceDataWrapper> resultList = new ArrayList<>();




        int totalSize = 0;
        int index = 0;
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            // serving/9999/batchInference
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (StringUtils.isNotBlank(value)) {
                String[] arr = value.trim().split("\\s+");
                for (String u : arr) {
                    URL url = URL.valueOf(u);
                    if (!Constants.EMPTY_PROTOCOL.equals(url.getProtocol())) {
                        String[] split = key.split("/");
                        if(!filterSet.contains(split[2]))
                            continue;
                        ServiceDataWrapper wrapper = new ServiceDataWrapper();
                        wrapper.setUrl(url.toFullString());
                        wrapper.setProject(split[0]);
                        wrapper.setEnvironment(split[1]);
                        wrapper.setName(key);
                        wrapper.setCallName(split[2]);
                        wrapper.setHost(url.getHost());
                        wrapper.setPort(url.getPort());
                        wrapper.setRouterMode(String.valueOf(url.getParameter("router_mode")));
                        wrapper.setVersion(Long.parseLong(url.getParameter("version", "100")));
                        wrapper.setWeight(Integer.parseInt(url.getParameter("weight", "100")));
                        wrapper.setIndex(index);
                        wrapper.setNeedVerify(VerifyService.contains(wrapper.getCallName()));
                        resultList.add(wrapper);
                        index++;
                    }
                }
            }
        }

        totalSize = resultList.size();

        resultList = resultList.stream().sorted((Comparator.comparing(o -> (o.getProject() + o.getEnvironment())))).collect(Collectors.toList());
        int totalPage = (resultList.size() + pageSize - 1) / pageSize;
        if (page <= totalPage) {
            resultList = resultList.subList((page - 1) * pageSize, Math.min(page * pageSize, resultList.size()));
        }

        if (logger.isDebugEnabled()) {
            logger.info("registered services: {}", resultList);
        }

        Map data = Maps.newHashMap();
        data.put("total", totalSize);
        data.put("rows", resultList);
        return ReturnResult.build(StatusCode.SUCCESS, Dict.SUCCESS, data);
    }

    /**
     * 修改每个接口中的路由信息，权重信息
     * @param requestParams
     * @return
     * @throws Exception
     */
    @PostMapping("/service/update")
    public ReturnResult updateService(@RequestBody RequestParamWrapper requestParams) throws Exception {
        String host = requestParams.getHost();
        int port = requestParams.getPort();
        String url = requestParams.getUrl();
        String routerMode = requestParams.getRouterMode();
        Integer weight = requestParams.getWeight();
        Long version = requestParams.getVersion();

        if (logger.isDebugEnabled()) {
            logger.debug("try to update service");
        }

        Preconditions.checkArgument(StringUtils.isNotBlank(url), "parameter url is blank");

        logger.info("update url: {}, routerMode: {}, weight: {}, version: {}", url, routerMode, weight, version);

        CommonServiceGrpc.CommonServiceFutureStub commonServiceFutureStub = getCommonServiceFutureStub(host, port);
        CommonServiceProto.UpdateServiceRequest.Builder builder = CommonServiceProto.UpdateServiceRequest.newBuilder();

        builder.setUrl(url);
        if (StringUtils.isNotBlank(routerMode)) {
            builder.setRouterMode(routerMode);
        }

        if (weight != null) {
            builder.setWeight(weight);
        } else {
            builder.setWeight(-1);
        }

        if (version != null) {
            builder.setVersion(version);
        } else {
            builder.setVersion(-1);
        }

        ListenableFuture<CommonServiceProto.CommonResponse> future = commonServiceFutureStub.updateService(builder.build());

        CommonServiceProto.CommonResponse response = future.get(MetaInfo.PROPERTY_GRPC_TIMEOUT, TimeUnit.MILLISECONDS);

        ReturnResult result = new ReturnResult();
        result.setRetcode(response.getStatusCode());
        result.setRetmsg(response.getMessage());
        return result;
    }

    @PostMapping("/service/updateFlowRule")
    public ReturnResult updateFlowRule(@RequestBody RequestParamWrapper requestParams) throws Exception {
        String source = requestParams.getSource();
        Integer allowQps = requestParams.getAllowQps();

        Preconditions.checkArgument(StringUtils.isNotBlank(source), "parameter source is blank");
        Preconditions.checkArgument(allowQps != null, "parameter allowQps is null");

        logger.info("update source: {}, allowQps: {}", source, allowQps);

        CommonServiceGrpc.CommonServiceFutureStub commonServiceFutureStub = getCommonServiceFutureStub(requestParams.getHost(), requestParams.getPort());
        CommonServiceProto.UpdateFlowRuleRequest.Builder builder = CommonServiceProto.UpdateFlowRuleRequest.newBuilder();

        builder.setSource(source);
        builder.setAllowQps(allowQps);

        ListenableFuture<CommonServiceProto.CommonResponse> future = commonServiceFutureStub.updateFlowRule(builder.build());

        CommonServiceProto.CommonResponse response = future.get(MetaInfo.PROPERTY_GRPC_TIMEOUT, TimeUnit.MILLISECONDS);

        ReturnResult result = new ReturnResult();
        result.setRetcode(response.getStatusCode());
        result.setRetmsg(response.getMessage());
        return result;
    }

    private CommonServiceGrpc.CommonServiceFutureStub getCommonServiceFutureStub(String host, Integer port) throws Exception {
        Preconditions.checkArgument(StringUtils.isNotBlank(host), "parameter host is blank");
        Preconditions.checkArgument(port != null && port.intValue() != 0, "parameter port was wrong");

        NetAddressChecker.check(host, port);

        ManagedChannel managedChannel = grpcConnectionPool.getManagedChannel(host, port);
        CommonServiceGrpc.CommonServiceFutureStub futureStub = CommonServiceGrpc.newFutureStub(managedChannel);
        return futureStub;
    }

}
