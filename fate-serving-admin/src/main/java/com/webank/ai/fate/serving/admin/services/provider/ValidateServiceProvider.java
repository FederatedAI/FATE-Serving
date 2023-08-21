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

package com.webank.ai.fate.serving.admin.services.provider;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.ByteString;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceGrpc;
import com.webank.ai.fate.api.serving.InferenceServiceGrpc;
import com.webank.ai.fate.api.serving.InferenceServiceProto;
import com.webank.ai.fate.serving.admin.controller.ValidateController;
import com.webank.ai.fate.serving.admin.services.AbstractAdminServiceProvider;
import com.webank.ai.fate.serving.admin.services.ComponentService;
import com.webank.ai.fate.serving.admin.utils.NetAddressChecker;
import com.webank.ai.fate.serving.common.rpc.core.FateService;
import com.webank.ai.fate.serving.common.rpc.core.FateServiceMethod;
import com.webank.ai.fate.serving.common.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.exceptions.BaseException;
import com.webank.ai.fate.serving.core.exceptions.RemoteRpcException;
import com.webank.ai.fate.serving.core.exceptions.SysException;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import com.webank.ai.fate.serving.core.utils.NetUtils;
import io.grpc.ManagedChannel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@FateService(name = "validateService", preChain = {
        "validateParamInterceptor"
})
@Service
public class ValidateServiceProvider extends AbstractAdminServiceProvider {

    private static final Logger logger = LoggerFactory.getLogger(ValidateController.class);
    GrpcConnectionPool grpcConnectionPool = GrpcConnectionPool.getPool();

    @Autowired
    ComponentService componentService;

    /*@FateServiceMethod(name = "publishLoad")
    public Object publishLoad(Context context, InboundPackage data) throws Exception {
        Map params = (Map) data.getBody();
        String host = (String) params.get(Dict.HOST);
        int port = (int) params.get(Dict.PORT);

        ModelServiceProto.PublishRequest publishRequest = buildPublishRequest(params);
        ModelServiceGrpc.ModelServiceFutureStub modelServiceFutureStub = getModelServiceFutureStub(host, port);
        ListenableFuture<ModelServiceProto.PublishResponse> future = modelServiceFutureStub.publishLoad(publishRequest);
        ModelServiceProto.PublishResponse response = future.get(timeout * 2, TimeUnit.MILLISECONDS);

        Map returnResult = new HashMap();
        returnResult.put(Dict.RET_CODE, response.getStatusCode());
        returnResult.put(Dict.RET_MSG, response.getMessage());
        return returnResult;
    }

    @FateServiceMethod(name = {"publishBind", "publishOnline"})
    public Object publishBind(Context context, InboundPackage data) throws Exception {
        Map params = (Map) data.getBody();
        String host = (String) params.get(Dict.HOST);
        int port = (int) params.get(Dict.PORT);

        Preconditions.checkArgument(StringUtils.isNotBlank((String) params.get("serviceId")), "parameter serviceId is required");

        ModelServiceProto.PublishRequest publishRequest = buildPublishRequest(params);
        ModelServiceGrpc.ModelServiceFutureStub modelServiceFutureStub = getModelServiceFutureStub(host, port);
        ListenableFuture<ModelServiceProto.PublishResponse> future = modelServiceFutureStub.publishBind(publishRequest);
        ModelServiceProto.PublishResponse response = future.get(timeout * 2, TimeUnit.MILLISECONDS);

        Map returnResult = new HashMap();
        returnResult.put(Dict.RET_CODE, response.getStatusCode());
        returnResult.put(Dict.RET_MSG, response.getMessage());
        return returnResult;
    }*/

    @FateServiceMethod(name = "inference")
    public Object inference(Context context, InboundPackage data) throws Exception {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String caseId = request.getHeader("caseId");

        Map params = (Map) data.getBody();
        String host = (String) params.get(Dict.HOST);
        int port = (int) params.get(Dict.PORT);

        String serviceId = (String) params.get("serviceId");
        Map<String, Object> featureData = (Map<String, Object>) params.get("featureData");
        Map<String, Object> sendToRemoteFeatureData = (Map<String, Object>) params.get("sendToRemoteFeatureData");
        try {
            Preconditions.checkArgument(StringUtils.isNotBlank(serviceId), "parameter serviceId is required");
            Preconditions.checkArgument(featureData != null, "parameter featureData is required");
            Preconditions.checkArgument(sendToRemoteFeatureData != null, "parameter sendToRemoteFeatureData is required");
        }
        catch(Exception e){
            throw new BaseException(StatusCode.GUEST_PARAM_ERROR, e.getMessage());
        }
        InferenceRequest inferenceRequest = new InferenceRequest();

        if (StringUtils.isNotBlank(serviceId)) {
            inferenceRequest.setServiceId(serviceId);
        }
        if(params.get("applyId")!=null){
            inferenceRequest.setApplyId(params.get("applyId").toString());
        }

        if(caseId != null && !caseId.isEmpty()) {
            inferenceRequest.setCaseId(caseId);
        }

        for (Map.Entry<String, Object> entry : featureData.entrySet()) {
            inferenceRequest.getFeatureData().put(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, Object> entry : sendToRemoteFeatureData.entrySet()) {
            inferenceRequest.getSendToRemoteFeatureData().put(entry.getKey(), entry.getValue());
        }

        InferenceServiceProto.InferenceMessage.Builder builder = InferenceServiceProto.InferenceMessage.newBuilder();
        builder.setBody(ByteString.copyFrom(JsonUtil.object2Json(inferenceRequest), Charset.forName("UTF-8")));

        InferenceServiceGrpc.InferenceServiceFutureStub inferenceServiceFutureStub = getInferenceServiceFutureStub(host, port);
        ListenableFuture<InferenceServiceProto.InferenceMessage> future = inferenceServiceFutureStub.inference(builder.build());
        InferenceServiceProto.InferenceMessage response = future.get(MetaInfo.PROPERTY_GRPC_TIMEOUT * 2, TimeUnit.MILLISECONDS);

        return JsonUtil.json2Object(response.getBody().toStringUtf8(), Map.class);
    }

    @FateServiceMethod(name = "batchInference")
    public Object batchInference(Context context, InboundPackage data) throws Exception {
        Map params = (Map) data.getBody();
        String host = (String) params.get(Dict.HOST);
        int port = (int) params.get(Dict.PORT);

        String serviceId = (String) params.get("serviceId");
        List<Map> batchDataList = (List<Map>) params.get("batchDataList");

        Preconditions.checkArgument(StringUtils.isNotBlank(serviceId), "parameter serviceId is required");
        Preconditions.checkArgument(batchDataList != null, "parameter batchDataList is required");

        BatchInferenceRequest batchInferenceRequest = new BatchInferenceRequest();

        if (StringUtils.isNotBlank(serviceId)) {
            batchInferenceRequest.setServiceId(serviceId);
        }

        List<BatchInferenceRequest.SingleInferenceData> singleInferenceDataList = Lists.newArrayList();
        for (int i = 0; i < batchDataList.size(); i++) {
            BatchInferenceRequest.SingleInferenceData singleInferenceData = new BatchInferenceRequest.SingleInferenceData();
            Map map = batchDataList.get(i);

            Map<String, Object> featureData = (Map<String, Object>) map.get("featureData");
            Map<String, Object> sendToRemoteFeatureData = (Map<String, Object>) map.get("sendToRemoteFeatureData");
            Preconditions.checkArgument(featureData != null, "parameter featureData is required");
            Preconditions.checkArgument(sendToRemoteFeatureData != null, "parameter sendToRemoteFeatureData is required");

            for (Map.Entry<String, Object> entry : featureData.entrySet()) {
                singleInferenceData.getFeatureData().put(entry.getKey(), entry.getValue());
            }

            for (Map.Entry<String, Object> entry : sendToRemoteFeatureData.entrySet()) {
                singleInferenceData.getSendToRemoteFeatureData().put(entry.getKey(), entry.getValue());
            }

            singleInferenceData.setIndex(i);
            singleInferenceDataList.add(singleInferenceData);
        }
        batchInferenceRequest.setBatchDataList(singleInferenceDataList);

        InferenceServiceProto.InferenceMessage.Builder builder = InferenceServiceProto.InferenceMessage.newBuilder();
        builder.setBody(ByteString.copyFrom(JsonUtil.object2Json(batchInferenceRequest), Charset.forName("UTF-8")));

        InferenceServiceGrpc.InferenceServiceFutureStub inferenceServiceFutureStub = getInferenceServiceFutureStub(host, port);
        ListenableFuture<InferenceServiceProto.InferenceMessage> future = inferenceServiceFutureStub.batchInference(builder.build());
        InferenceServiceProto.InferenceMessage response = future.get(MetaInfo.PROPERTY_GRPC_TIMEOUT * 2, TimeUnit.MILLISECONDS);

        return JsonUtil.json2Object(response.getBody().toStringUtf8(), Map.class);
    }

    /*private ModelServiceProto.PublishRequest buildPublishRequest(Map params) {
        Map<String, String> local = (Map<String, String>) params.get("local");
        Map<String, Map> roleMap = (Map<String, Map>) params.get("role");
        Map<String, Map> modelMap = (Map<String, Map>) params.get("model");
        String serviceId = (String) params.get("serviceId");
        String tableName = (String) params.get("tableName");
        String namespace = (String) params.get("namespace");
        String loadType = (String) params.get("loadType");
        String filePath = (String) params.get("filePath");

        Preconditions.checkArgument(local != null, "parameter local is required");
        Preconditions.checkArgument(roleMap != null, "parameter roleMap is required");
        Preconditions.checkArgument(modelMap != null, "parameter modelMap is required");

        ModelServiceProto.PublishRequest.Builder builder = ModelServiceProto.PublishRequest.newBuilder();
        builder.setLocal(ModelServiceProto.LocalInfo.newBuilder().setRole(local.get(Dict.ROLE)).setPartyId(local.get(Dict.PARTY_ID)).build());

        for (Map.Entry<String, Map> entry : roleMap.entrySet()) {
            Map value = entry.getValue();
            Preconditions.checkArgument(value != null);

            builder.putRole(entry.getKey(), ModelServiceProto.Party.newBuilder().addPartyId((String) value.get(Dict.PARTY_ID)).build());
        }

        for (Map.Entry<String, Map> entry : modelMap.entrySet()) {
            Map roleModelInfoMap = entry.getValue();
            Preconditions.checkArgument(roleModelInfoMap != null);

            if (roleModelInfoMap.size() == 1) {
                ModelServiceProto.RoleModelInfo roleModelInfo = null;

                Map.Entry<String, Map> roleModelInfoEntry = (Map.Entry<String, Map>) roleModelInfoMap.entrySet().iterator().next();
                Preconditions.checkArgument(roleModelInfoEntry != null);

                Map modelInfo = roleModelInfoEntry.getValue();
                roleModelInfo = ModelServiceProto.RoleModelInfo.newBuilder().putRoleModelInfo(roleModelInfoEntry.getKey(),
                        ModelServiceProto.ModelInfo.newBuilder().setTableName((String) modelInfo.get("tableName"))
                                .setNamespace((String) modelInfo.get("namespace")).build()).build();

                builder.putModel(entry.getKey(), roleModelInfo);
            }
        }

        if (StringUtils.isNotBlank(loadType)) {
            builder.setLoadType(loadType);
        }

        if (StringUtils.isNotBlank(filePath)) {
            builder.setFilePath(filePath);
        }

        if (StringUtils.isNotBlank(serviceId)) {
            builder.setServiceId(StringUtils.trim(serviceId));
        }

        return builder.build();
    }*/

    @Override
    protected Object transformExceptionInfo(Context context, ExceptionInfo data) {
        Map returnResult = new HashMap();
        if (data != null) {
            int code = data.getCode();
            String msg = data.getMessage() != null ? data.getMessage() : "";
            returnResult.put(Dict.RET_CODE, code);
            returnResult.put(Dict.RET_MSG, msg);
            return returnResult;
        }
        return null;
    }

    private ModelServiceGrpc.ModelServiceBlockingStub getModelServiceBlockingStub(String host, Integer port) throws Exception {
        NetAddressChecker.check(host, port);

        ManagedChannel managedChannel = grpcConnectionPool.getManagedChannel(host, port);
        ModelServiceGrpc.ModelServiceBlockingStub blockingStub = ModelServiceGrpc.newBlockingStub(managedChannel);
        blockingStub = blockingStub.withDeadlineAfter(MetaInfo.PROPERTY_GRPC_TIMEOUT, TimeUnit.MILLISECONDS);
        return blockingStub;
    }

    private ModelServiceGrpc.ModelServiceFutureStub getModelServiceFutureStub(String host, Integer port) throws Exception {
        NetAddressChecker.check(host, port);

        ManagedChannel managedChannel = grpcConnectionPool.getManagedChannel(host, port);
        return ModelServiceGrpc.newFutureStub(managedChannel);
    }

    private InferenceServiceGrpc.InferenceServiceFutureStub getInferenceServiceFutureStub(String host, Integer port) throws Exception {
        NetAddressChecker.check(host, port);

        ManagedChannel managedChannel = grpcConnectionPool.getManagedChannel(host, port);
        return InferenceServiceGrpc.newFutureStub(managedChannel);
    }
}
