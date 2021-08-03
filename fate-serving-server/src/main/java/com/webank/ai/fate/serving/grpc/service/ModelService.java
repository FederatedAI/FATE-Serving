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

package com.webank.ai.fate.serving.grpc.service;

import com.google.protobuf.ByteString;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceGrpc;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceProto;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceProto.PublishRequest;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceProto.PublishResponse;
import com.webank.ai.fate.register.annotions.RegisterService;
import com.webank.ai.fate.serving.common.bean.ServingServerContext;
import com.webank.ai.fate.serving.common.provider.ModelServiceProvider;
import com.webank.ai.fate.serving.common.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.common.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.ModelActionType;
import com.webank.ai.fate.serving.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ModelService extends ModelServiceGrpc.ModelServiceImplBase {

    @Autowired
    ModelServiceProvider modelServiceProvider;
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    @RegisterService(serviceName = "publishLoad")
    public synchronized void publishLoad(PublishRequest req, StreamObserver<PublishResponse> responseObserver) {
        Context context = prepareContext(ModelActionType.MODEL_LOAD.name());
        InboundPackage<PublishRequest> inboundPackage = new InboundPackage();
        inboundPackage.setBody(req);
        OutboundPackage outboundPackage = modelServiceProvider.service(context, inboundPackage);
        ReturnResult returnResult = (ReturnResult) outboundPackage.getData();
        logger.info("PublishRequest = {}",req);
        PublishResponse.Builder builder = PublishResponse.newBuilder();
        builder.setStatusCode(Integer.valueOf(returnResult.getRetcode()));
        builder.setMessage(returnResult.getRetmsg() != null ? returnResult.getRetmsg() : "");
        builder.setData(ByteString.copyFrom(JsonUtil.object2Json(returnResult.getData()).getBytes()));
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    @RegisterService(serviceName = "publishOnline")
    public synchronized void publishOnline(PublishRequest req, StreamObserver<PublishResponse> responseObserver) {
        Context context = prepareContext(ModelActionType.MODEL_PUBLISH_ONLINE.name());
        InboundPackage<ModelServiceProto.PublishRequest> inboundPackage = new InboundPackage();
        inboundPackage.setBody(req);
        OutboundPackage outboundPackage = modelServiceProvider.service(context, inboundPackage);
        ReturnResult returnResult = (ReturnResult) outboundPackage.getData();
        PublishResponse.Builder builder = PublishResponse.newBuilder();
        builder.setStatusCode(Integer.valueOf(returnResult.getRetcode()));
        builder.setMessage(returnResult.getRetmsg() != null ? returnResult.getRetmsg() : "");
        builder.setData(ByteString.copyFrom(JsonUtil.object2Json(returnResult.getData()).getBytes()));
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    @RegisterService(serviceName = "publishBind")
    public synchronized void publishBind(PublishRequest req, StreamObserver<PublishResponse> responseObserver) {
        Context context = prepareContext(ModelActionType.MODEL_PUBLISH_ONLINE.name());
        InboundPackage<ModelServiceProto.PublishRequest> inboundPackage = new InboundPackage();
        inboundPackage.setBody(req);
        OutboundPackage outboundPackage = modelServiceProvider.service(context, inboundPackage);
        ReturnResult returnResult = (ReturnResult) outboundPackage.getData();
        PublishResponse.Builder builder = PublishResponse.newBuilder();
        builder.setStatusCode(Integer.valueOf(returnResult.getRetcode()));
        builder.setMessage(returnResult.getRetmsg() != null ? returnResult.getRetmsg() : "");
        builder.setData(ByteString.copyFrom(JsonUtil.object2Json(returnResult.getData()).getBytes()));
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    @RegisterService(serviceName = "unload")
    public synchronized void unload(ModelServiceProto.UnloadRequest request, StreamObserver<ModelServiceProto.UnloadResponse> responseObserver) {
        Context context = prepareContext(ModelActionType.UNLOAD.name());
        InboundPackage<ModelServiceProto.UnloadRequest> inboundPackage = new InboundPackage();
        inboundPackage.setBody(request);
        OutboundPackage outboundPackage = modelServiceProvider.service(context, inboundPackage);
        ModelServiceProto.UnloadResponse unloadResponse = (ModelServiceProto.UnloadResponse) outboundPackage.getData();
        responseObserver.onNext(unloadResponse);
        responseObserver.onCompleted();
    }

    @Override
    @RegisterService(serviceName = "unbind")
    public synchronized void unbind(ModelServiceProto.UnbindRequest request, StreamObserver<ModelServiceProto.UnbindResponse> responseObserver) {
        Context context = prepareContext(ModelActionType.UNBIND.name());
        InboundPackage<ModelServiceProto.UnbindRequest> inboundPackage = new InboundPackage();
        inboundPackage.setBody(request);
        OutboundPackage outboundPackage = modelServiceProvider.service(context, inboundPackage);
        ModelServiceProto.UnbindResponse unbindResponse = (ModelServiceProto.UnbindResponse) outboundPackage.getData();
        responseObserver.onNext(unbindResponse);
        responseObserver.onCompleted();
    }

    @Override
    @RegisterService(serviceName = "fetchModel")
    public void fetchModel(ModelServiceProto.FetchModelRequest request, StreamObserver<ModelServiceProto.FetchModelResponse> responseObserver) {
        Context context = prepareContext(ModelActionType.FETCH_MODEL.name());
        InboundPackage<ModelServiceProto.FetchModelRequest> inboundPackage = new InboundPackage();
        inboundPackage.setBody(request);
        OutboundPackage outboundPackage = modelServiceProvider.service(context, inboundPackage);
        ModelServiceProto.FetchModelResponse fetchModelResponse = (ModelServiceProto.FetchModelResponse) outboundPackage.getData();
        responseObserver.onNext(fetchModelResponse);
        responseObserver.onCompleted();
    }

    @Override
    @RegisterService(serviceName = "modelTransfer")
    public void modelTransfer(ModelServiceProto.ModelTransferRequest request, StreamObserver<ModelServiceProto.ModelTransferResponse> responseObserver) {
        Context context = prepareContext(ModelActionType.MODEL_TRANSFER.name());
        InboundPackage<ModelServiceProto.ModelTransferRequest> inboundPackage = new InboundPackage();
        inboundPackage.setBody(request);
        OutboundPackage outboundPackage = modelServiceProvider.service(context, inboundPackage);
        ModelServiceProto.ModelTransferResponse modelTransferResponse = (ModelServiceProto.ModelTransferResponse) outboundPackage.getData();
        responseObserver.onNext(modelTransferResponse);
        responseObserver.onCompleted();
    }

    @Override
    @RegisterService(serviceName = "queryModel")
    public void queryModel(ModelServiceProto.QueryModelRequest request, StreamObserver<ModelServiceProto.QueryModelResponse> responseObserver) {
        Context context = prepareContext(ModelActionType.QUERY_MODEL.name());
        InboundPackage<ModelServiceProto.QueryModelRequest> inboundPackage = new InboundPackage();
        inboundPackage.setBody(request);
        OutboundPackage outboundPackage = modelServiceProvider.service(context, inboundPackage);
         ModelServiceProto.QueryModelResponse queryModelResponse = (ModelServiceProto.QueryModelResponse) outboundPackage.getData();
        responseObserver.onNext(queryModelResponse);
        responseObserver.onCompleted();
    }




    private Context prepareContext(String actionType) {
        ServingServerContext context = new ServingServerContext();
        context.setActionType(actionType);
        context.setCaseId(UUID.randomUUID().toString().replaceAll("-", ""));
        return context;
    }
}
