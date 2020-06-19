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

package com.webank.ai.fate.serving.proxy.security;

import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.serving.common.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.common.rpc.core.Interceptor;
import com.webank.ai.fate.serving.common.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.exceptions.InvalidRoleInfoException;
import com.webank.ai.fate.serving.core.rpc.grpc.GrpcType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 检查用户权限
 */
@Service
public class DefaultAuthentication implements Interceptor {

    Logger logger = LoggerFactory.getLogger(DefaultAuthentication.class);

    @Autowired
    private AuthUtils authUtils;

    @Override
    public void doPreProcess(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) throws Exception {
        if (GrpcType.INTRA_GRPC == context.getGrpcType()) {
            return;
        }

        Proxy.Packet sourcePackage = (Proxy.Packet) inboundPackage.getBody();
        boolean isAuthPass = authUtils.checkAuthentication(sourcePackage);
        if (!isAuthPass) {
            logger.error("invalid signature");
            throw new InvalidRoleInfoException();
        }
    }

    @Override
    public void doPostProcess(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) throws Exception {

    }
}
