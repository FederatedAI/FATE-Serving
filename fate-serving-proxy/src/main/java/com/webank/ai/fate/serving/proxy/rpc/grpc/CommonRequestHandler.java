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

package com.webank.ai.fate.serving.proxy.rpc.grpc;

import com.webank.ai.fate.api.networking.common.CommonServiceGrpc;
import com.webank.ai.fate.api.networking.common.CommonServiceProto;
import com.webank.ai.fate.register.annotions.RegisterService;
import com.webank.ai.fate.serving.common.bean.BaseContext;
import com.webank.ai.fate.serving.common.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.common.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.bean.CommonActionType;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.proxy.rpc.provider.CommonServiceProvider;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CommonRequestHandler extends CommonServiceGrpc.CommonServiceImplBase {

    private static final String QUERY_METRICS = "queryMetrics";
    private static final String UPDATE_FLOW_RULE = "updateFlowRule";
    private static final String LIST_PROPS = "listProps";
    private static final String QUERY_JVM = "queryJvm";
    private static final String UPDATE_SERVICE = "updateService";
    @Autowired
    CommonServiceProvider commonServiceProvider;

    @Override
    @RegisterService(serviceName = QUERY_METRICS)
    public void queryMetrics(CommonServiceProto.QueryMetricRequest request, StreamObserver<CommonServiceProto.CommonResponse> responseObserver) {
        Context context = prepareContext(CommonActionType.QUERY_METRICS.name());
        InboundPackage inboundPackage = new InboundPackage();
        inboundPackage.setBody(request);
        OutboundPackage outboundPackage = commonServiceProvider.service(context, inboundPackage);
        CommonServiceProto.CommonResponse response = (CommonServiceProto.CommonResponse) outboundPackage.getData();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    @RegisterService(serviceName = UPDATE_FLOW_RULE)
    public void updateFlowRule(CommonServiceProto.UpdateFlowRuleRequest request, StreamObserver<CommonServiceProto.CommonResponse> responseObserver) {
        Context context = prepareContext(CommonActionType.UPDATE_FLOW_RULE.name());
        InboundPackage inboundPackage = new InboundPackage();
        inboundPackage.setBody(request);
        OutboundPackage outboundPackage = commonServiceProvider.service(context, inboundPackage);
        CommonServiceProto.CommonResponse response = (CommonServiceProto.CommonResponse) outboundPackage.getData();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    @RegisterService(serviceName = LIST_PROPS)
    public void listProps(CommonServiceProto.QueryPropsRequest request, StreamObserver<CommonServiceProto.CommonResponse> responseObserver) {
        Context context = prepareContext(CommonActionType.LIST_PROPS.name());
        InboundPackage inboundPackage = new InboundPackage();
        inboundPackage.setBody(request);
        OutboundPackage outboundPackage = commonServiceProvider.service(context, inboundPackage);
        CommonServiceProto.CommonResponse response = (CommonServiceProto.CommonResponse) outboundPackage.getData();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    @RegisterService(serviceName = QUERY_JVM)
    public void queryJvmInfo(CommonServiceProto.QueryJvmInfoRequest request, StreamObserver<CommonServiceProto.CommonResponse> responseObserver) {
        Context context = prepareContext(CommonActionType.QUERY_JVM.name());
        InboundPackage inboundPackage = new InboundPackage();
        inboundPackage.setBody(request);
        OutboundPackage outboundPackage = commonServiceProvider.service(context, inboundPackage);
        CommonServiceProto.CommonResponse response = (CommonServiceProto.CommonResponse) outboundPackage.getData();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    @RegisterService(serviceName = UPDATE_SERVICE)
    public void updateService(CommonServiceProto.UpdateServiceRequest request, StreamObserver<CommonServiceProto.CommonResponse> responseObserver) {
        Context context = prepareContext(CommonActionType.UPDATE_SERVICE.name());
        InboundPackage inboundPackage = new InboundPackage();
        inboundPackage.setBody(request);
        OutboundPackage outboundPackage = commonServiceProvider.service(context, inboundPackage);
        CommonServiceProto.CommonResponse response = (CommonServiceProto.CommonResponse) outboundPackage.getData();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private Context prepareContext(String actionType) {
        BaseContext context = new BaseContext();
        context.setActionType(actionType);
        context.setCaseId(UUID.randomUUID().toString().replaceAll("-", ""));
        return context;
    }

}
