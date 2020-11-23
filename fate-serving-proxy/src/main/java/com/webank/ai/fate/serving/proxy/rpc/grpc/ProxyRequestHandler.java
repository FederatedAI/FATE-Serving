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

import com.webank.ai.fate.api.networking.proxy.DataTransferServiceGrpc;
import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.register.annotions.RegisterService;
import com.webank.ai.fate.serving.common.bean.BaseContext;
import com.webank.ai.fate.serving.common.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.common.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.common.rpc.core.ServiceAdaptor;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.proxy.rpc.core.ProxyServiceRegister;
import io.grpc.stub.StreamObserver;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ProxyRequestHandler extends DataTransferServiceGrpc.DataTransferServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(ProxyRequestHandler.class);

    public abstract ProxyServiceRegister getProxyServiceRegister();

    public abstract void setExtraInfo(Context context, InboundPackage<Proxy.Packet> inboundPackage, Proxy.Packet req);

    @RegisterService(serviceName = "unaryCall")
    @Override
    public void unaryCall(Proxy.Packet req, StreamObserver<Proxy.Packet> responseObserver) {

        if (logger.isDebugEnabled()) {
            logger.debug("unaryCall req {}", req);
        }
        ServiceAdaptor unaryCallService = getProxyServiceRegister().getServiceAdaptor("unaryCall");
        Context context = new BaseContext();
        InboundPackage<Proxy.Packet> inboundPackage = buildInboundPackage(context, req);
        setExtraInfo(context, inboundPackage, req);

        OutboundPackage<Proxy.Packet> outboundPackage = null;
        outboundPackage = unaryCallService.service(context, inboundPackage);
        Proxy.Packet result = (Proxy.Packet) outboundPackage.getData();
        responseObserver.onNext(result);
        responseObserver.onCompleted();
    }

    public InboundPackage<Proxy.Packet> buildInboundPackage(Context context, Proxy.Packet req) {
        context.setCaseId(Long.toString(System.currentTimeMillis()));
        if (StringUtils.isNotBlank(req.getHeader().getOperator())) {
            context.setVersion(req.getHeader().getOperator());
        }
        context.setGuestAppId(req.getHeader().getSrc().getPartyId());
        context.setHostAppid(req.getHeader().getDst().getPartyId());
        InboundPackage<Proxy.Packet> inboundPackage = new InboundPackage<Proxy.Packet>();
        inboundPackage.setBody(req);

        return inboundPackage;
    }
}