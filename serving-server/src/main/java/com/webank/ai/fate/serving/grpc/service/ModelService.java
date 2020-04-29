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


import com.alibaba.fastjson.JSONObject;
import com.codahale.metrics.MetricRegistry;
import com.google.protobuf.ByteString;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceGrpc;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceProto;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceProto.PublishRequest;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceProto.PublishResponse;
import com.webank.ai.fate.register.annotions.RegisterService;
import com.webank.ai.fate.serving.common.provider.ModelServiceProvider;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.utils.ObjectTransform;
import com.webank.ai.fate.serving.model.ModelManager;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class ModelService extends ModelServiceGrpc.ModelServiceImplBase implements /*InitializingBean,*/EnvironmentAware {

    private static final Logger logger = LoggerFactory.getLogger(ModelService.class);

    @Autowired
    ModelManager modelManager;
    @Autowired
    ModelServiceProvider modelServiceProvider;
    @Autowired
    MetricRegistry metricRegistry;

    Environment environment;

    @Override
    @RegisterService(serviceName = "publishLoad")
    public synchronized void publishLoad(PublishRequest req, StreamObserver<PublishResponse> responseObserver) {
        Context context = new BaseContext(new BaseLoggerPrinter(), ModelActionType.MODEL_LOAD.name(), metricRegistry);
        InboundPackage<ModelServiceProto.PublishRequest> inboundPackage = new InboundPackage();
        inboundPackage.setBody(req);
        context.setActionType("MODEL_LOAD");

        OutboundPackage outboundPackage = modelServiceProvider.service(context, inboundPackage);
        ReturnResult returnResult = (ReturnResult) outboundPackage.getData();

        PublishResponse.Builder builder = PublishResponse.newBuilder();
        builder.setStatusCode(Integer.valueOf(returnResult.getRetcode()));
        builder.setMessage(returnResult.getRetmsg());
        builder.setData(ByteString.copyFrom(JSONObject.toJSONString(returnResult.getData()).getBytes()));

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    @RegisterService(serviceName = "publishOnline")
    public synchronized void publishOnline(PublishRequest req, StreamObserver<PublishResponse> responseObserver) {
        Context context = new BaseContext(new BaseLoggerPrinter(), ModelActionType.MODEL_PUBLISH_ONLINE.name(), metricRegistry);
        InboundPackage<ModelServiceProto.PublishRequest> inboundPackage = new InboundPackage();
        inboundPackage.setBody(req);
        context.setActionType("MODEL_PUBLISH_ONLINE");
        OutboundPackage outboundPackage = modelServiceProvider.service(context, inboundPackage);
        ReturnResult returnResult = (ReturnResult) outboundPackage.getData();

        PublishResponse.Builder builder = PublishResponse.newBuilder();
        builder.setStatusCode(Integer.valueOf(returnResult.getRetcode()));
        builder.setMessage(returnResult.getRetmsg());
        builder.setData(ByteString.copyFrom(JSONObject.toJSONString(returnResult.getData()).getBytes()));

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    @RegisterService(serviceName = "publishBind")
    public synchronized void publishBind(PublishRequest req, StreamObserver<PublishResponse> responseObserver) {
        Context context = new BaseContext(new BaseLoggerPrinter(), ModelActionType.MODEL_PUBLISH_ONLINE.name(), metricRegistry);
        InboundPackage<ModelServiceProto.PublishRequest> inboundPackage = new InboundPackage();
        inboundPackage.setBody(req);
        context.setActionType("MODEL_PUBLISH_ONLINE");
        OutboundPackage outboundPackage = modelServiceProvider.service(context, inboundPackage);
        ReturnResult returnResult = (ReturnResult) outboundPackage.getData();

        PublishResponse.Builder builder = PublishResponse.newBuilder();
        builder.setStatusCode(Integer.valueOf(returnResult.getRetcode()));
        builder.setMessage(returnResult.getRetmsg());
        builder.setData(ByteString.copyFrom(JSONObject.toJSONString(returnResult.getData()).getBytes()));

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    @RegisterService(serviceName = "unload")
    public synchronized void unload(PublishRequest request, StreamObserver<PublishResponse> responseObserver) {
        Context context = new BaseContext(new BaseLoggerPrinter(), ModelActionType.UNLOAD.name(), metricRegistry);
        context.preProcess();
        ReturnResult returnResult = null;
        try {
            PublishResponse.Builder builder = PublishResponse.newBuilder();
            if (logger.isDebugEnabled()) {
                logger.debug("unload model table name: {}, namespace: {}", request.getTableName(), request.getNamespace());
            }

            returnResult = modelManager.unload(request.getTableName(), request.getNamespace());

            builder.setStatusCode(Integer.valueOf(returnResult.getRetcode()))
                    .setMessage(returnResult.getRetmsg())
                    .setData(ByteString.copyFrom(ObjectTransform.bean2Json(returnResult.getData()).getBytes()));
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } finally {
            context.postProcess(request, returnResult);
        }
    }

    @Override
    @RegisterService(serviceName = "unbind")
    public synchronized void unbind(ModelServiceProto.UnbindRequest request, StreamObserver<ModelServiceProto.UnbindResponse> responseObserver) {
        Context context = new BaseContext(new BaseLoggerPrinter(), ModelActionType.UNBIND.name(), metricRegistry);
        InboundPackage<ModelServiceProto.UnbindRequest> inboundPackage = new InboundPackage();
        OutboundPackage outboundPackage = modelServiceProvider.service(context, inboundPackage);
        ModelServiceProto.UnbindResponse unbindResponse = (ModelServiceProto.UnbindResponse) outboundPackage.getData();
        responseObserver.onNext(unbindResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void queryModel(com.webank.ai.fate.api.mlmodel.manager.ModelServiceProto.QueryModelRequest request,
                           io.grpc.stub.StreamObserver<com.webank.ai.fate.api.mlmodel.manager.ModelServiceProto.QueryModelResponse> responseObserver) {
        Context context = new BaseContext(new BaseLoggerPrinter(), ModelActionType.QUERY_MODEL.name(), metricRegistry);
        InboundPackage<ModelServiceProto.QueryModelRequest> inboundPackage = new InboundPackage();
        inboundPackage.setBody(request);
        context.setActionType("QUERY_MODEL");
        OutboundPackage outboundPackage = modelServiceProvider.service(context, inboundPackage);
        ModelServiceProto.QueryModelResponse queryModelResponse = (ModelServiceProto.QueryModelResponse) outboundPackage.getData();
        responseObserver.onNext(queryModelResponse);
        responseObserver.onCompleted();
    }


    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
