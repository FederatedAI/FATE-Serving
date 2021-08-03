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
import com.webank.ai.fate.api.serving.InferenceServiceGrpc;
import com.webank.ai.fate.api.serving.InferenceServiceProto;
import com.webank.ai.fate.api.serving.InferenceServiceProto.InferenceMessage;
import com.webank.ai.fate.register.annotions.RegisterService;
import com.webank.ai.fate.register.common.Role;
import com.webank.ai.fate.serving.common.bean.ServingServerContext;
import com.webank.ai.fate.serving.common.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.common.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.bean.BatchInferenceResult;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.utils.ObjectTransform;
import com.webank.ai.fate.serving.core.utils.ThreadPoolUtil;
import com.webank.ai.fate.serving.guest.provider.GuestBatchInferenceProvider;
import com.webank.ai.fate.serving.guest.provider.GuestSingleInferenceProvider;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadPoolExecutor;

@Service
public class GuestInferenceService extends InferenceServiceGrpc.InferenceServiceImplBase {
    static final String BATCH_INFERENCE = "batchInference";
    static final String INFERENCE = "inference";
    private static ThreadPoolExecutor executor = ThreadPoolUtil.newThreadPoolExecutor();
    @Autowired
    GuestBatchInferenceProvider guestBatchInferenceProvider;
    @Autowired
    GuestSingleInferenceProvider guestSingleInferenceProvider;

    @Override
    @RegisterService(useDynamicEnvironment = true, serviceName = INFERENCE,role = Role.GUEST)
    public void inference(InferenceMessage req, StreamObserver<InferenceMessage> responseObserver) {
        executor.submit(() -> {
            InferenceMessage.Builder response = InferenceMessage.newBuilder();
            Context context = prepareContext();
            InboundPackage inboundPackage = new InboundPackage();
            inboundPackage.setBody(req);
            OutboundPackage outboundPackage = this.guestSingleInferenceProvider.service(context, inboundPackage);
            ReturnResult returnResult = (ReturnResult) outboundPackage.getData();
            response.setBody(ByteString.copyFrom(ObjectTransform.bean2Json(returnResult).getBytes()));
            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        });
    }

    @Override
    @RegisterService(useDynamicEnvironment = true, serviceName = BATCH_INFERENCE,role = Role.GUEST)
    public void batchInference(InferenceServiceProto.InferenceMessage req, StreamObserver<InferenceServiceProto.InferenceMessage> responseObserver) {
        executor.submit(() -> {
            InferenceMessage.Builder response = InferenceMessage.newBuilder();
            Context context = prepareContext();
            InboundPackage inboundPackage = new InboundPackage();
            inboundPackage.setBody(req);
            OutboundPackage outboundPackage = this.guestBatchInferenceProvider.service(context, inboundPackage);
            BatchInferenceResult returnResult = (BatchInferenceResult) outboundPackage.getData();
            response.setBody(ByteString.copyFrom(ObjectTransform.bean2Json(returnResult).getBytes()));
            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        });
    }

    private Context prepareContext() {
        ServingServerContext context = new ServingServerContext();
        return context;
    }

}
