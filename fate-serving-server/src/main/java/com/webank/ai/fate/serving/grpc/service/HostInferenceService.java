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
import com.webank.ai.fate.api.networking.proxy.DataTransferServiceGrpc;
import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.api.networking.proxy.Proxy.Packet;
import com.webank.ai.fate.register.annotions.RegisterService;
import com.webank.ai.fate.register.common.Role;
import com.webank.ai.fate.serving.common.bean.ServingServerContext;
import com.webank.ai.fate.serving.common.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.common.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import com.webank.ai.fate.serving.core.utils.ThreadPoolUtil;
import com.webank.ai.fate.serving.host.provider.HostBatchInferenceProvider;
import com.webank.ai.fate.serving.host.provider.HostSingleInferenceProvider;
import io.grpc.stub.StreamObserver;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class HostInferenceService extends DataTransferServiceGrpc.DataTransferServiceImplBase {
    private static ThreadPoolExecutor executor = ThreadPoolUtil.newThreadPoolExecutor();
    @Autowired
    HostBatchInferenceProvider hostBatchInferenceProvider;
    @Autowired
    HostSingleInferenceProvider hostSingleInferenceProvider;

    @Override
    @RegisterService(serviceName = Dict.UNARYCALL, useDynamicEnvironment = true, role = Role.HOST)
    public void unaryCall(Proxy.Packet req, StreamObserver<Proxy.Packet> responseObserver) {
        executor.submit(() -> {
            String actionType = req.getHeader().getCommand().getName();
            ServingServerContext context = (ServingServerContext) prepareContext();
            String namespace = req.getHeader().getTask().getModel().getNamespace();
            String tableName = req.getHeader().getTask().getModel().getTableName();
            context.setActionType(actionType);
            context.setVersion(req.getHeader().getOperator());
            if (StringUtils.isBlank(context.getVersion()) || Double.parseDouble(context.getVersion()) < 200) {
                // 1.x
                Map hostFederatedParams = JsonUtil.json2Object(req.getBody().getValue().toStringUtf8(), Map.class);
                Map partnerModelInfo = (Map) hostFederatedParams.get("partnerModelInfo");
                namespace = partnerModelInfo.get("namespace").toString();
                tableName = partnerModelInfo.get("name").toString();
            }

            context.setModelNamesapce(namespace);
            context.setModelTableName(tableName);
            context.setCaseId(req.getAuth().getNonce());
            Object result = null;
            byte[] data = req.getBody().getValue().toByteArray();

            InboundPackage inboundPackage = new InboundPackage();
            switch (actionType) {
                case Dict.FEDERATED_INFERENCE:
                    context.setActionType(Dict.FEDERATED_INFERENCE);
                    inboundPackage.setBody(data);
                    OutboundPackage singleInferenceOutbound = hostSingleInferenceProvider.service(context, inboundPackage);
                    result = singleInferenceOutbound.getData();
                    break;
                case Dict.FEDERATED_INFERENCE_FOR_TREE:
                    context.setActionType(Dict.FEDERATED_INFERENCE_FOR_TREE);
                    inboundPackage.setBody(data);
                    OutboundPackage secureBoostTreeOutboundPackage = hostSingleInferenceProvider.service(context, inboundPackage);
                    result = secureBoostTreeOutboundPackage.getData();
                    break;
                case Dict.REMOTE_METHOD_BATCH:
                    inboundPackage.setBody(data);
                    OutboundPackage outboundPackage = this.hostBatchInferenceProvider.service(context, inboundPackage);
                    ReturnResult responseResult = null;
                    result = (ReturnResult) outboundPackage.getData();
                    break;
                default:
                    responseResult = new ReturnResult();
                    responseResult.setRetcode(StatusCode.HOST_UNSUPPORTED_COMMAND_ERROR);
                    break;
            }
            Packet.Builder packetBuilder = Packet.newBuilder();
            packetBuilder.setBody(Proxy.Data.newBuilder()
                    .setValue(ByteString.copyFrom(JsonUtil.object2Json(result).getBytes()))
                    .build());
            responseObserver.onNext(packetBuilder.build());
            responseObserver.onCompleted();
        });
    }

    private Context prepareContext() {
        ServingServerContext context = new ServingServerContext();
        return context;
    }
}