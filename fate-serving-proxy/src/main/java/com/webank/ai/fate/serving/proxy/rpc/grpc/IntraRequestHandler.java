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

import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.serving.common.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.rpc.grpc.GrpcType;
import com.webank.ai.fate.serving.proxy.rpc.core.ProxyServiceRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IntraRequestHandler extends ProxyRequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(IntraRequestHandler.class);

    @Autowired
    ProxyServiceRegister proxyServiceRegister;

    @Override
    public ProxyServiceRegister getProxyServiceRegister() {
        return proxyServiceRegister;
    }

    @Override
    public void setExtraInfo(Context context, InboundPackage<Proxy.Packet> inboundPackage, Proxy.Packet req) {
        context.setGrpcType(GrpcType.INTRA_GRPC);
    }

}


