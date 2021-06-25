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

import com.webank.ai.fate.register.annotions.RegisterService;
import com.webank.ai.fate.serving.common.bean.BaseContext;
import com.webank.ai.fate.serving.common.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.common.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.bean.CommonActionType;
import com.webank.ai.fate.serving.proxy.rpc.provider.RouterTableServiceProvider;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RouterTableService extends RouterTableServiceGrpc.RouterTableServiceImplBase {

    @Autowired
    RouterTableServiceProvider RouterTableServiceProvider;

    @Override
    @RegisterService(serviceName = "queryRouter")
    public synchronized void queryRouter(RouterTableServiceProto.RouterOperatetRequest request, StreamObserver<RouterTableServiceProto.RouterOperatetResponse> responseObserver) {
        service(request, responseObserver, CommonActionType.QUERY_ROUTER.name());
    }

    @Override
    @RegisterService(serviceName = "addRouter")
    public void addRouter(RouterTableServiceProto.RouterOperatetRequest request, StreamObserver<RouterTableServiceProto.RouterOperatetResponse> responseObserver) {
        service(request, responseObserver, CommonActionType.ADD_ROUTER.name());
    }

    @Override
    @RegisterService(serviceName = "updateRouter")
    public void updateRouter(RouterTableServiceProto.RouterOperatetRequest request, StreamObserver<RouterTableServiceProto.RouterOperatetResponse> responseObserver) {
        service(request, responseObserver, CommonActionType.UPDATE_ROUTER.name());
    }

    @Override
    @RegisterService(serviceName = "deleteRouter")
    public void deleteRouter(RouterTableServiceProto.RouterOperatetRequest request, StreamObserver<RouterTableServiceProto.RouterOperatetResponse> responseObserver) {
        service(request, responseObserver, CommonActionType.DELETE_ROUTER.name());
    }

    private void service(RouterTableServiceProto.RouterOperatetRequest request, StreamObserver<RouterTableServiceProto.RouterOperatetResponse> responseObserver, String actionType) {
        BaseContext context = new BaseContext();
        context.setActionType(actionType);
        context.setCaseId(UUID.randomUUID().toString().replaceAll("-", ""));
        InboundPackage inboundPackage = new InboundPackage();
        inboundPackage.setBody(request);
        OutboundPackage outboundPackage = RouterTableServiceProvider.service(context, inboundPackage);
        RouterTableServiceProto.RouterOperatetResponse response = (RouterTableServiceProto.RouterOperatetResponse) outboundPackage.getData();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
