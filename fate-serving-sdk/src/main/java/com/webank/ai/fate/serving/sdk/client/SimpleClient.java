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

package com.webank.ai.fate.serving.sdk.client;

import com.google.common.base.Preconditions;
import com.google.protobuf.ByteString;
import com.webank.ai.fate.api.serving.InferenceServiceGrpc;
import com.webank.ai.fate.api.serving.InferenceServiceProto;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import io.grpc.ManagedChannel;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.TimeUnit;

public class SimpleClient {

    static final String PROJECT = "serving";
    static final String SINGLE_INFERENCE = "inference";
    static final String BATCH_INFERENCE = "batchInference";
    static final String ClIENT_PROJECT = "client";
    static final int port = 36578;
    static final String ENVIRONMENT = "online";
    int  timeout = 3000;

    protected GrpcConnectionPool grpcConnectionPool = GrpcConnectionPool.getPool();

    public  SimpleClient setTimeout(int timeoutMs){
        Preconditions.checkArgument(this.timeout>0, "timeout must bigger than 0 ms");
        this.timeout = timeoutMs;
        return this;

    }

    public ReturnResult singleInference(String host, int port, InferenceRequest inferenceRequest) throws Exception {
        return singleInference(host,port,inferenceRequest,timeout);
    }
    public ReturnResult singleInference(String host, int port, InferenceRequest inferenceRequest,int timeout) throws Exception {
        Preconditions.checkArgument(inferenceRequest != null, "inferenceRequest is null");
        Preconditions.checkArgument(StringUtils.isNotEmpty(inferenceRequest.getServiceId()));
        InferenceServiceProto.InferenceMessage.Builder inferenceMessageBuilder = InferenceServiceProto.InferenceMessage.newBuilder();
        inferenceMessageBuilder.setBody(ByteString.copyFrom(JsonUtil.object2Json(inferenceRequest), "UTF-8"));
        InferenceServiceProto.InferenceMessage inferenceMessage = inferenceMessageBuilder.build();
        InferenceServiceGrpc.InferenceServiceBlockingStub blockingStub = this.getInferenceServiceBlockingStub(host, port,timeout);
        InferenceServiceProto.InferenceMessage result = blockingStub.inference(inferenceMessage);
        Preconditions.checkArgument(result != null && result.getBody() != null);
        return JsonUtil.deserializeJsonBytes(result.getBody().toByteArray(), ReturnResult.class);
    }


    public BatchInferenceResult batchInference(String host, int port, BatchInferenceRequest batchInferenceRequest) throws Exception {
        return  batchInference( host,  port,  batchInferenceRequest,  timeout);
    }


    public BatchInferenceResult batchInference(String host, int port, BatchInferenceRequest batchInferenceRequest,int  timeout) throws Exception {
        Preconditions.checkArgument(batchInferenceRequest != null, "batchInferenceRequest is null");
        Preconditions.checkArgument(StringUtils.isNotEmpty(batchInferenceRequest.getServiceId()));
        InferenceServiceProto.InferenceMessage.Builder inferenceMessageBuilder = InferenceServiceProto.InferenceMessage.newBuilder();
        inferenceMessageBuilder.setBody(ByteString.copyFrom(JsonUtil.object2Json(batchInferenceRequest), "UTF-8"));
        InferenceServiceGrpc.InferenceServiceBlockingStub blockingStub = this.getInferenceServiceBlockingStub(host, port, timeout);
        InferenceServiceProto.InferenceMessage result = blockingStub.batchInference(inferenceMessageBuilder.build());
        Preconditions.checkArgument(result != null && result.getBody() != null);
        return JsonUtil.deserializeJsonBytes(result.getBody().toByteArray(), BatchInferenceResult.class);
    }


    protected InferenceServiceGrpc.InferenceServiceBlockingStub getInferenceServiceBlockingStub(String host, int port,int timeout) throws Exception {
        Preconditions.checkArgument(StringUtils.isNotBlank(host), "host is blank");
        ManagedChannel managedChannel = grpcConnectionPool.getManagedChannel(host, port);
        InferenceServiceGrpc.InferenceServiceBlockingStub blockingStub = InferenceServiceGrpc.newBlockingStub(managedChannel);
        return blockingStub.withDeadlineAfter(timeout, TimeUnit.MILLISECONDS);
    }

}
