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
import com.google.common.util.concurrent.ListenableFuture;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceGrpc;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceProto;
import com.webank.ai.fate.serving.admin.services.ComponentService;
import com.webank.ai.fate.serving.admin.utils.NetAddressChecker;
import com.webank.ai.fate.serving.core.bean.GrpcConnectionPool;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import com.webank.ai.fate.serving.core.bean.RequestParamWrapper;
import com.webank.ai.fate.serving.core.bean.ReturnResult;
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

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description Model management
 * @Date: 2020/3/25 11:13
 * @Author: v_dylanxu
 */
@RequestMapping("/api")
@RestController
public class ModelController {

    private static final Logger logger = LoggerFactory.getLogger(ModelController.class);
    GrpcConnectionPool grpcConnectionPool = GrpcConnectionPool.getPool();

    @Autowired
    private ComponentService componentService;

    @GetMapping("/model/query")
    public ReturnResult queryModel(String host, Integer port, String serviceId,String tableName, String namespace, Integer page, Integer pageSize) throws Exception {
        Preconditions.checkArgument(StringUtils.isNotBlank(host), "parameter host is blank");
        Preconditions.checkArgument(port != 0, "parameter port is blank");

        int defaultPage = 1;
        int defaultPageSize = 10;

        if (page == null || page <= 0) {
            page = defaultPage;
        }

        if (pageSize == null || pageSize <= 0) {
            pageSize = defaultPageSize;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("query model, host: {}, port: {}, serviceId: {}", host, port, serviceId);
        }

        ModelServiceGrpc.ModelServiceBlockingStub blockingStub = getModelServiceBlockingStub(host, port);

        ModelServiceProto.QueryModelRequest.Builder queryModelRequestBuilder = ModelServiceProto.QueryModelRequest.newBuilder();

        if (StringUtils.isNotBlank(serviceId)) {
            // by service id
            queryModelRequestBuilder.setQueryType(1);
            queryModelRequestBuilder.setServiceId(serviceId);
        } else if (StringUtils.isNotBlank(namespace) && StringUtils.isNotBlank(tableName)) {
            // query model by tableName and namespace
            queryModelRequestBuilder.setTableName(tableName);
            queryModelRequestBuilder.setNamespace(namespace);
            queryModelRequestBuilder.setQueryType(2);
        } else {
            // list all
            queryModelRequestBuilder.setQueryType(0);
        }

        ModelServiceProto.QueryModelResponse response = blockingStub.queryModel(queryModelRequestBuilder.build());
        parseComponentInfo(response);
        if (logger.isDebugEnabled()) {
            logger.debug("response: {}", response);
        }

        //logger.info("response: {}", response);

        Map data = Maps.newHashMap();
        List rows = Lists.newArrayList();
        List<ModelServiceProto.ModelInfoEx> modelInfosList = response.getModelInfosList();
        int totalSize = 0;
        if (modelInfosList != null) {
            totalSize = modelInfosList.size();
            modelInfosList = modelInfosList.stream().sorted(Comparator.comparing(ModelServiceProto.ModelInfoEx::getTableName).reversed()).collect(Collectors.toList());

            // Pagination
            int totalPage = (modelInfosList.size() + pageSize - 1) / pageSize;
            if (page <= totalPage) {
                modelInfosList = modelInfosList.subList((page - 1) * pageSize, Math.min(page * pageSize, modelInfosList.size()));
            }

            for (ModelServiceProto.ModelInfoEx modelInfoEx : modelInfosList) {
                ModelServiceProto.ModelProcessorExt modelProcessorExt = modelInfoEx.getModelProcessorExt();
                Map modelData = JsonUtil.json2Object(modelInfoEx.getContent(), Map.class);
                List<Map>  componentList = Lists.newArrayList();
                if(modelProcessorExt!=null) {
                    List<ModelServiceProto.PipelineNode> nodes =  modelProcessorExt.getNodesList();
                    if(nodes!=null){
                        nodes.forEach(node ->{

                          Map paramMap = JsonUtil.json2Object(node.getParams(),Map.class)  ;
                          Map compnentMap =new HashMap();
                          compnentMap.put("name",node.getName());
                          compnentMap.put("params",paramMap);
                            componentList.add(compnentMap);
                        });
                    }

                    modelData.put("components",componentList);
                }
                rows.add(modelData);
            }
        }

        data.put("total", totalSize);
        data.put("rows", rows);

//        logger.info("=============={}",data);
        return ReturnResult.build(response.getRetcode(), response.getMessage(), data);
    }

    @PostMapping("/model/transfer")
    public Callable<ReturnResult> transfer(@RequestBody RequestParamWrapper requestParams) throws Exception {
        return () -> {
            String host = requestParams.getHost();
            Integer port = requestParams.getPort();
            String tableName = requestParams.getTableName();
            String namespace = requestParams.getNamespace();

            String targetHost = requestParams.getTargetHost();
            Integer targetPort = requestParams.getTargetPort();



            Preconditions.checkArgument(StringUtils.isNotBlank(tableName), "parameter tableName is blank");
            Preconditions.checkArgument(StringUtils.isNotBlank(namespace), "parameter namespace is blank");

            ReturnResult result = new ReturnResult();

            if (logger.isDebugEnabled()) {
                logger.debug("unload model by tableName and namespace, host: {}, port: {}, tableName: {}, namespace: {}", host, port, tableName, namespace);
            }

            ModelServiceGrpc.ModelServiceFutureStub futureStub = getModelServiceFutureStub(targetHost, targetPort);
            ModelServiceProto.FetchModelRequest   fetchModelRequest =  ModelServiceProto.FetchModelRequest.newBuilder()
                    //.setServiceId()
                    .setNamespace(namespace).setTableName(tableName).setSourceIp(host).setSourcePort(port).build();

            ListenableFuture<ModelServiceProto.FetchModelResponse> future = futureStub.fetchModel(fetchModelRequest);
            ModelServiceProto.FetchModelResponse response = future.get(MetaInfo.PROPERTY_GRPC_TIMEOUT, TimeUnit.MILLISECONDS);

            if (logger.isDebugEnabled()) {
                logger.debug("response: {}", response);
            }

            result.setRetcode(response.getStatusCode());
//            result.setData(JSONObject.parseObject(response.getData().toStringUtf8()));
            result.setRetmsg(response.getMessage());
            return result;
        };
    }



    @PostMapping("/model/unload")
    public Callable<ReturnResult> unload(@RequestBody RequestParamWrapper requestParams) throws Exception {
        return () -> {
            String host = requestParams.getHost();
            Integer port = requestParams.getPort();
            String tableName = requestParams.getTableName();
            String namespace = requestParams.getNamespace();

            Preconditions.checkArgument(StringUtils.isNotBlank(tableName), "parameter tableName is blank");
            Preconditions.checkArgument(StringUtils.isNotBlank(namespace), "parameter namespace is blank");

            ReturnResult result = new ReturnResult();

            if (logger.isDebugEnabled()) {
                logger.debug("unload model by tableName and namespace, host: {}, port: {}, tableName: {}, namespace: {}", host, port, tableName, namespace);
            }

            ModelServiceGrpc.ModelServiceFutureStub futureStub = getModelServiceFutureStub(host, port);

            ModelServiceProto.UnloadRequest unloadRequest = ModelServiceProto.UnloadRequest.newBuilder()
                    .setTableName(tableName)
                    .setNamespace(namespace)
                    .build();

            ListenableFuture<ModelServiceProto.UnloadResponse> future = futureStub.unload(unloadRequest);
            ModelServiceProto.UnloadResponse response = future.get(MetaInfo.PROPERTY_GRPC_TIMEOUT, TimeUnit.MILLISECONDS);

            if (logger.isDebugEnabled()) {
                logger.debug("response: {}", response);
            }

            result.setRetcode(response.getStatusCode());
//            result.setData(JSONObject.parseObject(response.getData().toStringUtf8()));
            result.setRetmsg(response.getMessage());
            return result;
        };
    }

    @PostMapping("/model/unbind")
    public Callable<ReturnResult> unbind(@RequestBody RequestParamWrapper requestParams) throws Exception {
        return () -> {
            String host = requestParams.getHost();
            Integer port = requestParams.getPort();
            String tableName = requestParams.getTableName();
            String namespace = requestParams.getNamespace();
            List<String> serviceIds = requestParams.getServiceIds();

            Preconditions.checkArgument(StringUtils.isNotBlank(tableName), "parameter tableName is blank");
            Preconditions.checkArgument(StringUtils.isNotBlank(namespace), "parameter namespace is blank");
            Preconditions.checkArgument(serviceIds != null && serviceIds.size() != 0, "parameter serviceId is blank");

            ReturnResult result = new ReturnResult();

            if (logger.isDebugEnabled()) {
                logger.debug("unload model by tableName and namespace, host: {}, port: {}, tableName: {}, namespace: {}", host, port, tableName, namespace);
            }

            ModelServiceGrpc.ModelServiceFutureStub futureStub = getModelServiceFutureStub(host, port);

            ModelServiceProto.UnbindRequest unbindRequest = ModelServiceProto.UnbindRequest.newBuilder()
                    .setTableName(tableName)
                    .setNamespace(namespace)
                    .addAllServiceIds(serviceIds)
                    .build();

            ListenableFuture<ModelServiceProto.UnbindResponse> future = futureStub.unbind(unbindRequest);
            ModelServiceProto.UnbindResponse response = future.get(MetaInfo.PROPERTY_GRPC_TIMEOUT, TimeUnit.MILLISECONDS);

            if (logger.isDebugEnabled()) {
                logger.debug("response: {}", response);
            }

            result.setRetcode(response.getStatusCode());
            result.setRetmsg(response.getMessage());
            return result;
        };
    }

    private ModelServiceGrpc.ModelServiceBlockingStub getModelServiceBlockingStub(String host, Integer port) throws Exception {
        Preconditions.checkArgument(StringUtils.isNotBlank(host), "parameter host is blank");
        Preconditions.checkArgument(port != null && port.intValue() != 0, "parameter port was wrong");

        NetAddressChecker.check(host, port);

        ManagedChannel managedChannel = grpcConnectionPool.getManagedChannel(host, port);
        ModelServiceGrpc.ModelServiceBlockingStub blockingStub = ModelServiceGrpc.newBlockingStub(managedChannel);
        blockingStub = blockingStub.withDeadlineAfter(MetaInfo.PROPERTY_GRPC_TIMEOUT, TimeUnit.MILLISECONDS);
        return blockingStub;
    }

    private ModelServiceGrpc.ModelServiceFutureStub getModelServiceFutureStub(String host, Integer port) throws Exception {
        Preconditions.checkArgument(StringUtils.isNotBlank(host), "parameter host is blank");
        Preconditions.checkArgument(port != null && port.intValue() != 0, "parameter port was wrong");

        NetAddressChecker.check(host, port);

        ManagedChannel managedChannel = grpcConnectionPool.getManagedChannel(host, port);
        return ModelServiceGrpc.newFutureStub(managedChannel);
    }

    public void parseComponentInfo(ModelServiceProto.QueryModelResponse response){

    }

}
