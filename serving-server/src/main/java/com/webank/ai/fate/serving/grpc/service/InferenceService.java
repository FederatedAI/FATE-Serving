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
import com.webank.ai.fate.api.serving.InferenceServiceGrpc;
import com.webank.ai.fate.api.serving.InferenceServiceProto;
import com.webank.ai.fate.api.serving.InferenceServiceProto.InferenceMessage;
import com.webank.ai.fate.register.annotions.RegisterService;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.constant.InferenceRetCode;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.utils.ObjectTransform;
import com.webank.ai.fate.serving.guest.provider.BatchGuestInferenceProvider;
import com.webank.ai.fate.serving.guest.provider.OldVersionInferenceProvider;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InferenceService extends InferenceServiceGrpc.InferenceServiceImplBase {
    private static final Logger logger = LoggerFactory.getLogger(InferenceService.class);
    @Autowired
    BatchGuestInferenceProvider batchGuestInferenceProvider;
    @Autowired
    OldVersionInferenceProvider oldVersionInferenceProvider;
    @Autowired
    MetricRegistry metricRegistry;
    @Override
    @RegisterService(useDynamicEnvironment = true, serviceName = "inference")
    public void inference(InferenceMessage req, StreamObserver<InferenceMessage> responseObserver) {
        InferenceMessage.Builder response = InferenceMessage.newBuilder();
        ReturnResult returnResult = new ReturnResult();
        Context  context = prepareContext();
        byte[] reqbody = req.getBody().toByteArray();
        InboundPackage inboundPackage = new InboundPackage();
        inboundPackage.setBody(reqbody);
        OutboundPackage outboundPackage = this.oldVersionInferenceProvider.service(context, inboundPackage);
        returnResult = (ReturnResult) outboundPackage.getData();
        response.setBody(ByteString.copyFrom(ObjectTransform.bean2Json(returnResult).getBytes()));
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }





    @Override
    @RegisterService(serviceName = "getInferenceResult" ,useDynamicEnvironment = true)
    @Deprecated
    public void getInferenceResult(InferenceMessage req, StreamObserver<InferenceMessage> responseObserver) {
        
    }

    @Override
    @RegisterService(useDynamicEnvironment = true, serviceName = "")
    public void startInferenceJob(InferenceMessage req, StreamObserver<InferenceMessage> responseObserver) {


    }

    @Override
    @RegisterService(useDynamicEnvironment = true, serviceName = "batchInference")
    public void batchInference(InferenceServiceProto.InferenceMessage req, StreamObserver<InferenceServiceProto.InferenceMessage> responseObserver) {
        InferenceMessage.Builder response = InferenceMessage.newBuilder();
        BatchInferenceResult returnResult = new BatchInferenceResult();
        Context  context = prepareContext();
        byte[] reqbody = req.getBody().toByteArray();
        InboundPackage inboundPackage = new InboundPackage();
        inboundPackage.setBody(reqbody);
        OutboundPackage outboundPackage = null;
        outboundPackage = this.batchGuestInferenceProvider.service(context, inboundPackage);
        returnResult = (BatchInferenceResult) outboundPackage.getData();
        response.setBody(ByteString.copyFrom(ObjectTransform.bean2Json(returnResult).getBytes()));
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();

    }


//    private void inferenceServiceAction(InferenceMessage req, StreamObserver<InferenceMessage> responseObserver, InferenceActionType actionType) {
//
//        InferenceMessage.Builder response = InferenceMessage.newBuilder();
//        ReturnResult returnResult = new ReturnResult();
//        InferenceRequest inferenceRequest = null;
//        Context context = new BaseContext(new GuestInferenceLoggerPrinter(), actionType.name(), metricRegistry);
//        context.setInterfaceName(Dict.EVENT_INFERENCE);
//        context.preProcess();
//
//        try {
//            try {
//                context.putData(Dict.ORIGIN_REQUEST, req.getBody().toStringUtf8());
//                inferenceRequest = (InferenceRequest) JSON.parseObject(req.getBody().toStringUtf8(), InferenceRequest.class);
//
//                // for async monitor test
////                DisruptorUtil.producer(Dict.EVENT_INFERENCE, actionType.name() + "_ERROR", inferenceRequest);
//
//                if (inferenceRequest != null) {
//                    if (inferenceRequest.getCaseid().length() == 0) {
//                        inferenceRequest.setCaseId(InferenceUtils.generateCaseid());
//                    }
//                    Map<String,Object> sendToRemoteFeatureData = inferenceRequest.getSendToRemoteFeatureData();
//                    if(sendToRemoteFeatureData!=null) {
//                        inferenceRequest.getFeatureData().putAll(sendToRemoteFeatureData);
//                    }
//                    context.setCaseId(inferenceRequest.getCaseid());
//
//
//                    switch (actionType.name()) {
//                        case "SYNC_RUN":
//                            returnResult = guestInferenceProvider.syncInference(context, inferenceRequest);
//                            break;
//                        case "GET_RESULT":
//                            returnResult = guestInferenceProvider.getResult(context, inferenceRequest);
//                            break;
//                        case "ASYNC_RUN":
//                            returnResult = guestInferenceProvider.asynInference(context, inferenceRequest);
//                            break;
//                        default:
//                            throw new Exception();
//                    }
//
//                    if (returnResult.getRetcode() != InferenceRetCode.OK) {
//                        logger.info("caseid {} inference {} failed: {}  result {}", context.getCaseId(), actionType, req.getBody().toStringUtf8(), returnResult);
//                    }
//                } else {
//
//                    returnResult.setRetcode(InferenceRetCode.EMPTY_DATA);
//                }
//            } catch (Throwable e) {
//                returnResult.setRetcode(InferenceRetCode.SYSTEM_ERROR);
//                logger.error(String.format("inference system error:\n%s", req.getBody().toStringUtf8()), e);
//            }
//
//            response.setBody(ByteString.copyFrom(ObjectTransform.bean2Json(returnResult).getBytes()));
//            responseObserver.onNext(response.build());
//            responseObserver.onCompleted();
//        } finally {
//
//            context.postProcess(inferenceRequest, returnResult);
//
//
//  }
//    }


    private  Context  prepareContext(){

        ServingServerContext context = new ServingServerContext();
        context.setMetricRegistry(this.metricRegistry);
        //context.preProcess();
        return  context;
    }


}
