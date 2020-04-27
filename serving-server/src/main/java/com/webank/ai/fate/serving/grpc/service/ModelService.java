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


import com.codahale.metrics.MetricRegistry;
import com.google.protobuf.ByteString;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceGrpc;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceProto;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceProto.PublishRequest;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceProto.PublishResponse;
import com.webank.ai.fate.register.annotions.RegisterService;
import com.webank.ai.fate.serving.common.provider.ModelServiceProvider;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.model.Model;
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

import java.util.List;

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
    public synchronized void publishLoad(PublishRequest req, StreamObserver<PublishResponse> responseStreamObserver) {

        Context context = new BaseContext(new BaseLoggerPrinter(), ModelActionType.MODEL_LOAD.name(), metricRegistry);

        context.preProcess();
        ReturnResult returnResult = null;

        try {
            PublishResponse.Builder builder = PublishResponse.newBuilder();
            context.putData(Dict.SERVICE_ID, req.getServiceId());

            returnResult = modelManager.load(context, req);
            /*returnResult = modelManager.publishLoadModel(context,
                    new FederatedParty(req.getLocal().getRole(), req.getLocal().getPartyId()),
                    ModelUtil.getFederatedRoles(req.getRoleMap()),
                    ModelUtil.getFederatedRolesModel(req.getModelMap()));*/
            builder.setStatusCode(Integer.valueOf(returnResult.getRetcode()))
                    .setMessage(returnResult.getRetmsg())
                    .setData(ByteString.copyFrom(ObjectTransform.bean2Json(returnResult.getData()).getBytes()));

             /*if (returnResult.getRetcode() == 0) {
               RequestWapper  requestWapper =new RequestWapper(new String(encoder.encode(req.toByteArray())),System.currentTimeMillis(),md5Crypt(req));
                publishLoadReqMap.put(requestWapper.md5,requestWapper);

                fireStoreEvent();
            }*/
            responseStreamObserver.onNext(builder.build());
            responseStreamObserver.onCompleted();
        } finally {
            context.postProcess(req, returnResult);
        }
    }

    @Override
    @RegisterService(serviceName = "publishOnline")
    public synchronized void publishOnline(PublishRequest req, StreamObserver<PublishResponse> responseStreamObserver) {
        Context context = new BaseContext(new BaseLoggerPrinter(), ModelActionType.MODEL_PUBLISH_ONLINE.name(), metricRegistry);
        context.preProcess();
        ReturnResult returnResult = null;
        try {
            PublishResponse.Builder builder = PublishResponse.newBuilder();
            context.putData(Dict.SERVICE_ID, req.getServiceId());
            if (logger.isDebugEnabled()) {
                logger.debug("receive service id {}", req.getServiceId());
            }


            returnResult = modelManager.bind(context, req);

            /*returnResult = modelManager.publishOnlineModel(context,
                    new FederatedParty(req.getLocal().getRole(), req.getLocal().getPartyId()),
                    ModelUtil.getFederatedRoles(req.getRoleMap()),
                    ModelUtil.getFederatedRolesModel(req.getModelMap())
            );*/
            builder.setStatusCode(Integer.valueOf(returnResult.getRetcode()))
                    .setMessage(returnResult.getRetmsg())
                    .setData(ByteString.copyFrom(ObjectTransform.bean2Json(returnResult.getData()).getBytes()));
            /*if (returnResult.getRetcode() == 0) {
                String content = new String(encoder.encode(req.toByteArray()));
                RequestWapper requestWapper = new RequestWapper(content,System.currentTimeMillis(),md5Crypt(req));
                publicOnlineReqMap.put(requestWapper.md5, requestWapper);
                fireStoreEvent();
            }*/
            responseStreamObserver.onNext(builder.build());
            responseStreamObserver.onCompleted();
        } finally {
            context.postProcess(req, returnResult);
        }
    }

    @Override
    @RegisterService(serviceName = "publishBind")
    public synchronized void publishBind(PublishRequest req, StreamObserver<PublishResponse> responseStreamObserver) {
        Context context = new BaseContext(new BaseLoggerPrinter(), ModelActionType.MODEL_PUBLISH_ONLINE.name(), metricRegistry);
        context.preProcess();
        ReturnResult returnResult = null;
        try {
            PublishResponse.Builder builder = PublishResponse.newBuilder();
            context.putData(Dict.SERVICE_ID, req.getServiceId());
            if (logger.isDebugEnabled()) {
                logger.debug("publishBind receive service id {}", context.getData(Dict.SERVICE_ID));
            }

            returnResult = modelManager.bind(context, req);

            /*returnResult = modelManager.publishOnlineModel(context,
                    new FederatedParty(req.getLocal().getRole(), req.getLocal().getPartyId()),
                    ModelUtil.getFederatedRoles(req.getRoleMap()),
                    ModelUtil.getFederatedRolesModel(req.getModelMap())
            );*/
            builder.setStatusCode(Integer.valueOf(returnResult.getRetcode()))
                    .setMessage(returnResult.getRetmsg())
                    .setData(ByteString.copyFrom(ObjectTransform.bean2Json(returnResult.getData()).getBytes()));
            /*if (returnResult.getRetcode() == 0) {

                String content = new String(encoder.encode(req.toByteArray()));

                try {
                    PublishRequest xx= PublishRequest.parseFrom(decoder.decode(content));

                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
                RequestWapper requestWapper = new RequestWapper(content,System.currentTimeMillis(),md5Crypt(req));
                publicOnlineReqMap.put(requestWapper.md5, requestWapper);
                fireStoreEvent();
            }*/
            responseStreamObserver.onNext(builder.build());
            responseStreamObserver.onCompleted();
        } finally {
            context.postProcess(req, returnResult);
        }
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

//        responseObserver.onNext(builder.build());
//        responseObserver.onCompleted();
//
//        context.preProcess();
//        ReturnResult returnResult = null;
//        try {
//            PublishResponse.Builder builder = PublishResponse.newBuilder();
//            if (logger.isDebugEnabled()) {
//                logger.debug("unbind receive service id: {}", request.getServiceId());
//            }
//
//            returnResult = modelManager.unbind(context, request);
//
//            builder.setStatusCode(Integer.valueOf(returnResult.getRetcode()))
//                    .setMessage(returnResult.getRetmsg())
//                    .setData(ByteString.copyFrom(ObjectTransform.bean2Json(returnResult.getData()).getBytes()));
//            responseObserver.onNext(builder.build());
//            responseObserver.onCompleted();
//        } finally {
//            context.postProcess(request, returnResult);
//        }
    }

    /*@Override
    @RegisterService(serviceName = "listAllModel")
    public void listAllModel(PublishRequest request, StreamObserver<PublishResponse> responseObserver) {
        Context context = new BaseContext(new BaseLoggerPrinter(), ModelActionType.LIST_ALL_MODEL.name(), metricRegistry);
        context.preProcess();
        ReturnResult returnResult = new ReturnResult();
        try {
            PublishResponse.Builder builder = PublishResponse.newBuilder();

            List<Model> models = modelManager.listAllModel();

            if (logger.isDebugEnabled()) {
                logger.debug("list all model： {}", models);
            }

            returnResult.setRetcode(StatusCode.SUCCESS);
            returnResult.setRetmsg(Dict.SUCCESS);

            builder.setStatusCode(Integer.valueOf(StatusCode.SUCCESS))
                    .setMessage(Dict.SUCCESS)
                    .setData(ByteString.copyFrom(ObjectTransform.bean2Json(models).getBytes()));
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } finally {
            context.postProcess(request, returnResult);
        }
    }*/


//    @RegisterService(serviceName = "getModelByTableNameAndNamespace")
//    public void getModelByTableNameAndNamespace(PublishRequest request, StreamObserver<PublishResponse> responseObserver) {
//        Context context = new BaseContext(new BaseLoggerPrinter(), ModelActionType.GET_MODEL_BY_TABLE_NAME_AND_NAMESPACE.name(), metricRegistry);
//        context.preProcess();
//        ReturnResult returnResult = new ReturnResult();
//        try {
//            PublishResponse.Builder builder = PublishResponse.newBuilder();
//            if (logger.isDebugEnabled()) {
//                logger.debug("get model by tableName: {}, namespace: {}", request.getTableName(), request.getNamespace());
//            }
//
//            Model model = modelManager.getModelByTableNameAndNamespace(request.getTableName(), request.getNamespace());
//
//            returnResult.setRetcode(StatusCode.OK);
//            returnResult.setRetmsg(Dict.SUCCESS);
//
//            builder.setStatusCode(StatusCode.OK)
//                    .setMessage(Dict.SUCCESS)
//                    .setData(ByteString.copyFrom(ObjectTransform.bean2Json(model).getBytes()));
//            responseObserver.onNext(builder.build());
//            responseObserver.onCompleted();
//        } finally {
//            context.postProcess(request, returnResult);
//        }
//    }

//    @Override
//    @RegisterService(serviceName = "getModelByServiceId")
//    public void getModelByServiceId(PublishRequest request, StreamObserver<PublishResponse> responseObserver) {
//        Context context = new BaseContext(new BaseLoggerPrinter(), ModelActionType.GET_MODEL_BY_SERVICE_ID.name(), metricRegistry);
//        context.preProcess();
//        ReturnResult returnResult = new ReturnResult();
//        try {
//            PublishResponse.Builder builder = PublishResponse.newBuilder();
//            if (logger.isDebugEnabled()) {
//                logger.debug("get model by service id: {}", request.getServiceId());
//            }
//            Model model = modelManager.getModelByServiceId(request.getServiceId());
//            returnResult.setRetcode(StatusCode.OK);
//            returnResult.setRetmsg(Dict.SUCCESS);
//            builder.setStatusCode(StatusCode.OK)
//                    .setMessage(Dict.SUCCESS)
//                    .setData(ByteString.copyFrom(ObjectTransform.bean2Json(model).getBytes()));
//            responseObserver.onNext(builder.build());
//            responseObserver.onCompleted();
//        } finally {
//            context.postProcess(request, returnResult);
//        }
//    }



    @Override
    public void queryModel(com.webank.ai.fate.api.mlmodel.manager.ModelServiceProto.QueryModelRequest request,
                           io.grpc.stub.StreamObserver<com.webank.ai.fate.api.mlmodel.manager.ModelServiceProto.QueryModelResponse> responseObserver) {
        Context context = new BaseContext(new BaseLoggerPrinter(), ModelActionType.UNBIND.name(), metricRegistry);
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
