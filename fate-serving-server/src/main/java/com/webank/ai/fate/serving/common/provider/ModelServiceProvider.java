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

package com.webank.ai.fate.serving.common.provider;

import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import com.google.protobuf.ProtocolStringList;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceGrpc;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceProto;
import com.webank.ai.fate.register.url.CollectionUtils;
import com.webank.ai.fate.serving.common.flow.FlowCounterManager;
import com.webank.ai.fate.serving.common.model.Model;
import com.webank.ai.fate.serving.common.rpc.core.FateService;
import com.webank.ai.fate.serving.common.rpc.core.FateServiceMethod;
import com.webank.ai.fate.serving.common.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.exceptions.SysException;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import com.webank.ai.fate.serving.core.utils.NetUtils;
import com.webank.ai.fate.serving.federatedml.PipelineModelProcessor;
import com.webank.ai.fate.serving.guest.provider.AbstractServingServiceProvider;
import com.webank.ai.fate.serving.model.ModelManager;
import io.grpc.ManagedChannel;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@FateService(name = "modelService", preChain = {
        "requestOverloadBreaker"
}, postChain = {
})
@Service
public class ModelServiceProvider extends AbstractServingServiceProvider {
    @Autowired
    ModelManager modelManager;

    @Autowired
    FlowCounterManager flowCounterManager;

    Logger logger = LoggerFactory.getLogger(ModelServiceProvider.class);

    GrpcConnectionPool grpcConnectionPool = GrpcConnectionPool.getPool();

    @FateServiceMethod(name = "MODEL_LOAD")
    public Object load(Context context, InboundPackage data) {
        ModelServiceProto.PublishRequest publishRequest = (ModelServiceProto.PublishRequest) data.getBody();
        ReturnResult returnResult = modelManager.load(context, publishRequest);
        return returnResult;
    }

    @FateServiceMethod(name = "MODEL_PUBLISH_ONLINE")
    public Object bind(Context context, InboundPackage data) {
        ModelServiceProto.PublishRequest req = (ModelServiceProto.PublishRequest) data.getBody();
        ReturnResult returnResult = modelManager.bind(context, req);
        return returnResult;
    }

    @FateServiceMethod(name = "QUERY_MODEL")
    public ModelServiceProto.QueryModelResponse queryModel(Context context, InboundPackage data) {
        ModelServiceProto.QueryModelRequest req = (ModelServiceProto.QueryModelRequest) data.getBody();
        List<Model> models = modelManager.queryModel(context, req);
        ModelServiceProto.QueryModelResponse.Builder builder = ModelServiceProto.QueryModelResponse.newBuilder();
        if (CollectionUtils.isNotEmpty(models)) {
            for (int i = 0; i < models.size(); i++) {
                Model model = models.get(i);
                if (model == null) {
                    continue;
                }

                List<Map> rolePartyMapList = model.getRolePartyMapList();
                if (rolePartyMapList == null) {
                    rolePartyMapList = new ArrayList<>();
                }

                Map rolePartyMap = new HashMap();
                rolePartyMap.put(Dict.ROLE, model.getRole());
                rolePartyMap.put(Dict.PART_ID, model.getPartId());
                rolePartyMapList.add(rolePartyMap);

                if (model.getFederationModelMap() != null) {
                    for (Model value : model.getFederationModelMap().values()) {
                        rolePartyMap = new HashMap();
                        rolePartyMap.put(Dict.ROLE, value.getRole());
                        rolePartyMap.put(Dict.PART_ID, value.getPartId());
                        rolePartyMapList.add(rolePartyMap);
                    }
                }

                model.setRolePartyMapList(rolePartyMapList);
                model.setAllowQps(flowCounterManager.getAllowedQps(model.getResourceName()));

                ModelServiceProto.ModelInfoEx.Builder modelExBuilder = ModelServiceProto.ModelInfoEx.newBuilder();
                modelExBuilder.setIndex(i);
                modelExBuilder.setTableName(model.getTableName());
                modelExBuilder.setNamespace(model.getNamespace());
                modelExBuilder.setResourceAdress(model.getResourceAdress() == null ? "" : model.getResourceAdress());
                if (model.getServiceIds() != null) {
                    modelExBuilder.addAllServiceIds(model.getServiceIds());
                }
                //JsonUtil.object2Json(model)
                Gson gson = new Gson();
                modelExBuilder.setContent(gson.toJson(model));
                if (model.getModelProcessor() instanceof PipelineModelProcessor) {
                    ModelServiceProto.ModelProcessorExt.Builder modelProcessorExtBuilder = ModelServiceProto.ModelProcessorExt.newBuilder();
                    PipelineModelProcessor pipelineModelProcessor = (PipelineModelProcessor) model.getModelProcessor();
//                    System.out.println(JsonUtil.object2Json(pipelineModelProcessor.getParmasMap()));
                    //pipelineModelProcessor.getPipeLineNode()
//                    modelProcessorExtBuilder.setDslParser(JsonUtil.object2Json(pipelineModelProcessor .getDslParser()));
//                    modelProcessorExtBuilder.setParamsMap(JsonUtil.object2Json(pipelineModelProcessor.getParmasMap()));
//                    modelProcessorExtBuilder.setPipelineNodes()

                    if (pipelineModelProcessor != null) {
                        pipelineModelProcessor.getPipeLineNode().forEach(node -> {
                            if (node != null) {
                                ModelServiceProto.PipelineNode.Builder pipelineNodeBuilder = ModelServiceProto.PipelineNode.newBuilder();
                                pipelineNodeBuilder.setName(node.getComponentName());
                                pipelineNodeBuilder.setParams(node.getParam() instanceof Message ? JsonUtil.pbToJson((Message) node.getParam()) : JsonUtil.object2Json(node.getParam()));
                                modelProcessorExtBuilder.addNodes(pipelineNodeBuilder.build());
                            }
                        });
                    }
                    modelExBuilder.setModelProcessorExt(modelProcessorExtBuilder.build());
                }
                builder.addModelInfos(modelExBuilder.build());
            }
        }
        builder.setRetcode(StatusCode.SUCCESS);

        return builder.build();
    }

    @FateServiceMethod(name = "UNLOAD")
    public ModelServiceProto.UnloadResponse unload(Context context, InboundPackage data) {
        ModelServiceProto.UnloadRequest req = (ModelServiceProto.UnloadRequest) data.getBody();
        ModelServiceProto.UnloadResponse res = modelManager.unload(context, req);
        return res;
    }

    @FateServiceMethod(name = "UNBIND")
    public ModelServiceProto.UnbindResponse unbind(Context context, InboundPackage data) {
        ModelServiceProto.UnbindRequest req = (ModelServiceProto.UnbindRequest) data.getBody();
        ModelServiceProto.UnbindResponse res = modelManager.unbind(context, req);
        return res;
    }

    @FateServiceMethod(name = "FETCH_MODEL")
    public ModelServiceProto.FetchModelResponse fetchModel(Context context, InboundPackage data) {
        ModelServiceProto.FetchModelRequest req = (ModelServiceProto.FetchModelRequest) data.getBody();

        ModelServiceProto.FetchModelResponse.Builder responseBuilder = ModelServiceProto.FetchModelResponse.newBuilder();
        if (!MetaInfo.PROPERTY_MODEL_SYNC) {
            return responseBuilder.setStatusCode(StatusCode.MODEL_SYNC_ERROR)
                    .setMessage("no synchronization allowed, to use this function, please configure model.synchronize=true").build();
        }

        String tableName = req.getTableName();
        String namespace = req.getNamespace();
        List<String> serviceIds = Lists.newArrayList();
        ProtocolStringList serviceIdsList = req.getServiceIdsList();
        ModelServiceProto.ModelTransferRequest.Builder modelTransferRequestBuilder = ModelServiceProto.ModelTransferRequest.newBuilder();
//        if(serviceIdsList!=null){
//            serviceIdsList.asByteStringList().forEach(serviceIdByte ->{
//                serviceIds.add(serviceIdByte.toString());
//            });
//        }

        String sourceIp = req.getSourceIp();
        int sourcePort = req.getSourcePort();
        if (!NetUtils.isValidAddress(sourceIp + ":" + sourcePort)) {
            throw new SysException("invalid address");
        }

        // check model exist
        Model model;
//        if (StringUtils.isNotBlank(serviceId)) {
//            model = modelManager.queryModel(serviceId);
//        } else {
//            model = modelManager.queryModel(tableName, namespace);
//        }
//        if (model != null) {
//            return responseBuilder.setStatusCode(StatusCode.MODEL_SYNC_ERROR)
//                    .setMessage("model already exists").build();
//        }

        ManagedChannel managedChannel = grpcConnectionPool.getManagedChannel(sourceIp, sourcePort);
        ModelServiceGrpc.ModelServiceBlockingStub blockingStub = ModelServiceGrpc.newBlockingStub(managedChannel)
                .withDeadlineAfter(MetaInfo.PROPERTY_GRPC_TIMEOUT, TimeUnit.MILLISECONDS);

        ModelServiceProto.ModelTransferRequest modelTransferRequest = modelTransferRequestBuilder
                .setTableName(tableName).setNamespace(namespace).build();

        ModelServiceProto.ModelTransferResponse modelTransferResponse = blockingStub.modelTransfer(modelTransferRequest);

        if (modelTransferResponse.getStatusCode() != StatusCode.SUCCESS) {
            return responseBuilder.setStatusCode(modelTransferResponse.getStatusCode()).setMessage(modelTransferResponse.getMessage()).build();
        }

        byte[] modelData = modelTransferResponse.getModelData().toByteArray();
        byte[] cacheData = modelTransferResponse.getCacheData().toByteArray();

        Model fetchModel = JsonUtil.json2Object(modelData, Model.class);

        modelManager.restoreByLocalCache(context, fetchModel, cacheData);

        return responseBuilder.setStatusCode(StatusCode.SUCCESS).setMessage(Dict.SUCCESS).build();
    }

    @FateServiceMethod(name = "MODEL_TRANSFER")
    public synchronized ModelServiceProto.ModelTransferResponse modelTransfer(Context context, InboundPackage data) {
        ModelServiceProto.ModelTransferRequest req = (ModelServiceProto.ModelTransferRequest) data.getBody();

        ModelServiceProto.ModelTransferResponse.Builder responseBuilder = ModelServiceProto.ModelTransferResponse.newBuilder();
        if (!MetaInfo.PROPERTY_MODEL_SYNC) {
            return responseBuilder.setStatusCode(StatusCode.MODEL_SYNC_ERROR)
                    .setMessage("no synchronization allowed, to use this function, please configure model.synchronize=true")
                    .build();
        }

        //String serviceId = req.getServiceId();
        String tableName = req.getTableName();
        String namespace = req.getNamespace();

        logger.info("model transfer by tableName：{}, namespace：{}", tableName, namespace);

        Model model;
//        if (StringUtils.isNotBlank(serviceId)) {
//            model = modelManager.queryModel(serviceId);
//        } else {
        model = modelManager.queryModel(tableName, namespace);

        //}
        if (model == null) {
            logger.error("model {}_{} is not exist", tableName, namespace);
//            throw new ModelNullException("model is not exist ");
            return responseBuilder.setStatusCode(StatusCode.MODEL_NULL)
                    .setMessage("model is not exist")
                    .build();
        }
        byte[] cacheData = modelManager.getModelCacheData(context, model.getTableName(), model.getNamespace());
        if (cacheData == null) {
            logger.error("model {}_{} cache data is null", model.getTableName(), model.getNamespace());
//            throw new ModelNullException("model cache data is null");
            return responseBuilder.setStatusCode(StatusCode.MODEL_NULL)
                    .setMessage("model cache data is null")
                    .build();
        }

        responseBuilder.setStatusCode(StatusCode.SUCCESS).setMessage(Dict.SUCCESS)
                .setModelData(ByteString.copyFrom(JsonUtil.object2Json(model).getBytes()))
                .setCacheData(ByteString.copyFrom(cacheData));

        return responseBuilder.build();
    }


    @Override
    protected Object transformExceptionInfo(Context context, ExceptionInfo data) {
        String actionType = context.getActionType();
        if (data != null) {
            int code = data.getCode();
            String msg = data.getMessage() != null ? data.getMessage().toString() : "";
            if (StringUtils.isNotEmpty(actionType)) {
                switch (actionType) {
                    case "MODEL_LOAD":
                        ;
                    case "MODEL_PUBLISH_ONLINE":
                        ReturnResult returnResult = new ReturnResult();
                        returnResult.setRetcode(code);
                        returnResult.setRetmsg(msg);
                        return returnResult;
                    case "QUERY_MODEL":
                        ModelServiceProto.QueryModelResponse queryModelResponse = ModelServiceProto.QueryModelResponse.newBuilder().setRetcode(code).setMessage(msg).build();
                        return queryModelResponse;
                    case "UNLOAD":
                        ModelServiceProto.UnloadResponse unloadResponse = ModelServiceProto.UnloadResponse.newBuilder().setStatusCode(code).setMessage(msg).build();
                        return unloadResponse;
                    case "UNBIND":
                        ModelServiceProto.UnbindResponse unbindResponse = ModelServiceProto.UnbindResponse.newBuilder().setStatusCode(code).setMessage(msg).build();
                        return unbindResponse;
                }

            }
        }
        return null;
    }

    @Override
    protected void printFlowLog(Context context) {

        flowLogger.info("{}|{}|{}|{}|{}|{}|{}|{}",

                context.getCaseId(), context.getReturnCode(), context.getCostTime(),
                context.getDownstreamCost(), serviceName, context.getRouterInfo() != null ? context.getRouterInfo() : "",
                MetaInfo.PROPERTY_PRINT_INPUT_DATA ? context.getData(Dict.INPUT_DATA) : "",
                MetaInfo.PROPERTY_PRINT_OUTPUT_DATA ? context.getData(Dict.OUTPUT_DATA) : ""
        );
    }
}
