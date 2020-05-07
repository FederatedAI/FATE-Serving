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
import com.webank.ai.fate.serving.core.bean.BatchInferenceResult;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.bean.ServingServerContext;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.utils.ObjectTransform;
import com.webank.ai.fate.serving.guest.provider.GuestBatchInferenceProvider;
import com.webank.ai.fate.serving.guest.provider.GuestSingleInferenceProvider;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class GuestInferenceService extends InferenceServiceGrpc.InferenceServiceImplBase {
    static final String BATCHINFERENCE = "batchInference";
    static final String INFERENCE = "inference";
    private static final Logger logger = LoggerFactory.getLogger(GuestInferenceService.class);
    @Autowired
    GuestBatchInferenceProvider guestBatchInferenceProvider;
    @Autowired
    GuestSingleInferenceProvider guestSingleInferenceProvider;
    @Autowired
    MetricRegistry metricRegistry;
    @Autowired
    Environment environment;

    @Override
    @RegisterService(useDynamicEnvironment = true, serviceName = INFERENCE)
    public void inference(InferenceMessage req, StreamObserver<InferenceMessage> responseObserver) {
        InferenceMessage.Builder response = InferenceMessage.newBuilder();
        ReturnResult returnResult = new ReturnResult();
        Context context = prepareContext(INFERENCE);
        byte[] reqbody = req.getBody().toByteArray();
        InboundPackage inboundPackage = new InboundPackage();
        inboundPackage.setBody(reqbody);
        OutboundPackage outboundPackage = this.guestSingleInferenceProvider.service(context, inboundPackage);
        returnResult = (ReturnResult) outboundPackage.getData();
        response.setBody(ByteString.copyFrom(ObjectTransform.bean2Json(returnResult).getBytes()));
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }


    @Override
    @RegisterService(serviceName = "getInferenceResult", useDynamicEnvironment = true)
    @Deprecated
    public void getInferenceResult(InferenceMessage req, StreamObserver<InferenceMessage> responseObserver) {

    }

    @Override
    @RegisterService(useDynamicEnvironment = true, serviceName = "startInferenceJob")
    public void startInferenceJob(InferenceMessage req, StreamObserver<InferenceMessage> responseObserver) {


    }

    @Override
    @RegisterService(useDynamicEnvironment = true, serviceName = BATCHINFERENCE)
    public void batchInference(InferenceServiceProto.InferenceMessage req, StreamObserver<InferenceServiceProto.InferenceMessage> responseObserver) {
        InferenceMessage.Builder response = InferenceMessage.newBuilder();
        Context context = prepareContext(BATCHINFERENCE);
        byte[] reqbody = req.getBody().toByteArray();
        InboundPackage inboundPackage = new InboundPackage();
        inboundPackage.setBody(reqbody);
        OutboundPackage outboundPackage = this.guestBatchInferenceProvider.service(context, inboundPackage);
        BatchInferenceResult returnResult = (BatchInferenceResult) outboundPackage.getData();
        response.setBody(ByteString.copyFrom(ObjectTransform.bean2Json(returnResult).getBytes()));
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();

    }

    private Context prepareContext(String interfaceName) {
        ServingServerContext context = new ServingServerContext();
        context.setMetricRegistry(this.metricRegistry);
        context.setEnvironment(environment);
        context.setInterfaceName(interfaceName);
        return context;
    }


}
