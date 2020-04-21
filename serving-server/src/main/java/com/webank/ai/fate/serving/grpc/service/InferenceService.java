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
import com.webank.ai.fate.serving.guest.provider.SingleGuestInferenceProvider;
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
    SingleGuestInferenceProvider singleGuestInferenceProvider;
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
        OutboundPackage outboundPackage = this.singleGuestInferenceProvider.service(context, inboundPackage);
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
    @RegisterService(useDynamicEnvironment = true, serviceName = "startInferenceJob")
    public void startInferenceJob(InferenceMessage req, StreamObserver<InferenceMessage> responseObserver) {


    }

    @Override
    @RegisterService(useDynamicEnvironment = true, serviceName = "batchInference")
    public void batchInference(InferenceServiceProto.InferenceMessage req, StreamObserver<InferenceServiceProto.InferenceMessage> responseObserver) {
        InferenceMessage.Builder response = InferenceMessage.newBuilder();
        Context  context = prepareContext();
        byte[] reqbody = req.getBody().toByteArray();
        InboundPackage inboundPackage = new InboundPackage();
        inboundPackage.setBody(reqbody);
        OutboundPackage outboundPackage = this.batchGuestInferenceProvider.service(context, inboundPackage);
        BatchInferenceResult returnResult = (BatchInferenceResult) outboundPackage.getData();
        response.setBody(ByteString.copyFrom(ObjectTransform.bean2Json(returnResult).getBytes()));
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();

    }



    private  Context  prepareContext(){

        ServingServerContext context = new ServingServerContext();
        context.setMetricRegistry(this.metricRegistry);
        //context.preProcess();
        return  context;
    }


}
