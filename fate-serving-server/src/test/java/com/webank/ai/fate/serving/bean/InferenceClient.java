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

package com.webank.ai.fate.serving.bean;

import com.google.protobuf.ByteString;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceGrpc;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceProto;
import com.webank.ai.fate.api.networking.common.CommonServiceGrpc;
import com.webank.ai.fate.api.networking.common.CommonServiceProto;
import com.webank.ai.fate.api.serving.InferenceServiceGrpc;
import com.webank.ai.fate.api.serving.InferenceServiceProto;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NegotiationType;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;

import java.util.concurrent.TimeUnit;

public class InferenceClient {

    protected String ip;
    protected Integer port;

    public InferenceClient(String ip, Integer port) {
        this.ip = ip;
        this.port = port;
    }

    public static ManagedChannel createManagedChannel(String ip, int port) throws Exception {
        NettyChannelBuilder builder = NettyChannelBuilder
                .forAddress(ip, port)
                .keepAliveTime(60, TimeUnit.SECONDS)
                .keepAliveTimeout(60, TimeUnit.SECONDS)
                .keepAliveWithoutCalls(true)
                .idleTimeout(60, TimeUnit.SECONDS)
                .perRpcBufferLimit(128 << 20)
                .flowControlWindow(32 << 20)
                .maxInboundMessageSize(32 << 20)
                .enableRetry()
                .retryBufferSize(16 << 20)
                .maxRetryAttempts(20);      // todo: configurable
        builder.negotiationType(NegotiationType.PLAINTEXT)
                .usePlaintext();

        return builder.build();
    }

    public CommonServiceProto.CommonResponse queryMetrics(CommonServiceProto.QueryMetricRequest data) {
        ManagedChannel managedChannel = null;
        try {
            managedChannel = createManagedChannel(ip, port);
        } catch (Exception e) {
            e.printStackTrace();
        }
        CommonServiceGrpc.CommonServiceBlockingStub blockingStub = CommonServiceGrpc.newBlockingStub(managedChannel);
        return blockingStub.queryMetrics(data);
    }

    public InferenceServiceProto.InferenceMessage batchInference(InferenceServiceProto.InferenceMessage data) {
        ManagedChannel managedChannel = null;
        try {
            managedChannel = createManagedChannel(ip, port);
        } catch (Exception e) {
            e.printStackTrace();
        }
        InferenceServiceGrpc.InferenceServiceBlockingStub blockingStub = InferenceServiceGrpc.newBlockingStub(managedChannel);
        return blockingStub.batchInference(data);
    }

    public InferenceServiceProto.InferenceMessage inference(byte[] data) {
        ManagedChannel managedChannel = null;
        try {
            managedChannel = createManagedChannel(ip, port);
        } catch (Exception e) {
            e.printStackTrace();
        }

        InferenceServiceGrpc.InferenceServiceBlockingStub blockingStub = InferenceServiceGrpc.newBlockingStub(managedChannel);
        InferenceServiceProto.InferenceMessage.Builder inferenceMessageBuilder =
                InferenceServiceProto.InferenceMessage.newBuilder();

        inferenceMessageBuilder.setBody(ByteString.copyFrom(data));

        return blockingStub.inference(inferenceMessageBuilder.build());
    }

    public InferenceServiceProto.InferenceMessage inference(InferenceServiceProto.InferenceMessage inferenceMessage) {
        ManagedChannel managedChannel = null;
        try {
            managedChannel = createManagedChannel(ip, port);
        } catch (Exception e) {
            e.printStackTrace();
        }

        InferenceServiceGrpc.InferenceServiceBlockingStub blockingStub = InferenceServiceGrpc.newBlockingStub(managedChannel);
        InferenceServiceProto.InferenceMessage.Builder inferenceMessageBuilder =
                InferenceServiceProto.InferenceMessage.newBuilder();

        return blockingStub.inference(inferenceMessage);
    }

    public ModelServiceProto.PublishResponse load(ModelServiceProto.PublishRequest publishRequest) {
        ManagedChannel managedChannel = null;
        try {
            managedChannel = createManagedChannel(ip, port);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ModelServiceGrpc.ModelServiceBlockingStub blockingStub = ModelServiceGrpc.newBlockingStub(managedChannel);
        return blockingStub.publishLoad(publishRequest);
    }

    public ModelServiceProto.PublishResponse bind(ModelServiceProto.PublishRequest publishRequest) {
        ManagedChannel managedChannel = null;
        try {
            managedChannel = createManagedChannel(ip, port);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ModelServiceGrpc.ModelServiceBlockingStub blockingStub = ModelServiceGrpc.newBlockingStub(managedChannel);
        return blockingStub.publishBind(publishRequest);
    }

    public CommonServiceProto.CommonResponse queryMetric(CommonServiceProto.QueryMetricRequest data) {
        ManagedChannel managedChannel = null;
        try {
            managedChannel = createManagedChannel(ip, port);
        } catch (Exception e) {
            e.printStackTrace();
        }
        CommonServiceGrpc.CommonServiceBlockingStub blockingStub = CommonServiceGrpc.newBlockingStub(managedChannel);
        return blockingStub.queryMetrics(data);
    }

    public CommonServiceProto.CommonResponse updateRule(CommonServiceProto.UpdateFlowRuleRequest data) {
        ManagedChannel managedChannel = null;
        try {
            managedChannel = createManagedChannel(ip, port);
        } catch (Exception e) {
            e.printStackTrace();
        }

        CommonServiceGrpc.CommonServiceBlockingStub blockingStub = CommonServiceGrpc.newBlockingStub(managedChannel);
        return blockingStub.updateFlowRule(data);
    }

    public CommonServiceProto.CommonResponse listProps(CommonServiceProto.QueryPropsRequest data) {
        ManagedChannel managedChannel = null;
        try {
            managedChannel = createManagedChannel(ip, port);
        } catch (Exception e) {
            e.printStackTrace();
        }

        CommonServiceGrpc.CommonServiceBlockingStub blockingStub = CommonServiceGrpc.newBlockingStub(managedChannel);
        return blockingStub.listProps(data);
    }

    public ModelServiceProto.QueryModelResponse queryModels(ModelServiceProto.QueryModelRequest data) {
        ManagedChannel managedChannel = null;
        try {
            managedChannel = createManagedChannel(ip, port);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ModelServiceGrpc.ModelServiceBlockingStub blockingStub = ModelServiceGrpc.newBlockingStub(managedChannel);
        return blockingStub.queryModel(data);
    }

    public CommonServiceProto.CommonResponse queryJvmInfo(CommonServiceProto.QueryJvmInfoRequest data) {
        ManagedChannel managedChannel = null;
        try {
            managedChannel = createManagedChannel(ip, port);
        } catch (Exception e) {
            e.printStackTrace();
        }

        CommonServiceGrpc.CommonServiceBlockingStub blockingStub = CommonServiceGrpc.newBlockingStub(managedChannel);
        return blockingStub.queryJvmInfo(data);
    }

    public CommonServiceProto.CommonResponse checkHealth(CommonServiceProto.HealthCheckRequest data) {
        ManagedChannel managedChannel = null;
        try {
            managedChannel = createManagedChannel(ip, port);
        } catch (Exception e) {
            e.printStackTrace();
        }

        CommonServiceGrpc.CommonServiceBlockingStub blockingStub = CommonServiceGrpc.newBlockingStub(managedChannel);
        return blockingStub.checkHealthService(data);
    }
}
