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

package com.webank.ai.fate.serving.proxy.rpc.services;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.ByteString;
import com.webank.ai.fate.api.networking.proxy.DataTransferServiceGrpc;
import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.serving.common.rpc.core.AbstractServiceAdaptor;
import com.webank.ai.fate.serving.common.rpc.core.FateService;
import com.webank.ai.fate.serving.common.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.common.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.GrpcConnectionPool;
import com.webank.ai.fate.serving.core.rpc.router.RouterInfo;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import com.webank.ai.fate.serving.proxy.security.AuthUtils;
import io.grpc.ManagedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Description TODO
 * @Author
 **/
// TODO utu: may load from cfg file is a better choice compare to using annotation?
@Service
@FateService(name = "unaryCall", preChain = {
        "requestOverloadBreaker",
        "federationParamValidator",
        "defaultAuthentication",
        "defaultServingRouter"})
public class UnaryCallService extends AbstractServiceAdaptor<Proxy.Packet, Proxy.Packet> {

    Logger logger = LoggerFactory.getLogger(UnaryCallService.class);

    GrpcConnectionPool grpcConnectionPool = GrpcConnectionPool.getPool();

    @Autowired
    AuthUtils authUtils;
    @Value("${proxy.grpc.unaryCall.timeout:3000}")
    private int timeout;

    @Override
    public Proxy.Packet doService(Context context, InboundPackage<Proxy.Packet> data, OutboundPackage<Proxy.Packet> outboundPackage) {
        try {
            Proxy.Packet sourcePackage = data.getBody();
            sourcePackage = authUtils.addAuthInfo(sourcePackage);

            RouterInfo routerInfo = data.getRouterInfo();
            ManagedChannel managedChannel = grpcConnectionPool.getManagedChannel(routerInfo.getHost(), routerInfo.getPort());
            DataTransferServiceGrpc.DataTransferServiceFutureStub stub1 = DataTransferServiceGrpc.newFutureStub(managedChannel);
            stub1.withDeadlineAfter(timeout, TimeUnit.MILLISECONDS);

            context.setDownstreamBegin(System.currentTimeMillis());

            ListenableFuture<Proxy.Packet> future = stub1.unaryCall(sourcePackage);
            Proxy.Packet packet = future.get(timeout, TimeUnit.MILLISECONDS);

            return packet;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("unaryCall error ", e);
        } finally {
            long end = System.currentTimeMillis();
            context.setDownstreamCost(end - context.getDownstreamBegin());
        }
        return null;
    }

    @Override
    protected Proxy.Packet transformExceptionInfo(Context context, ExceptionInfo exceptionInfo) {


        Proxy.Packet.Builder builder = Proxy.Packet.newBuilder();
        Proxy.Data.Builder dataBuilder = Proxy.Data.newBuilder();
        Map fateMap = Maps.newHashMap();
        fateMap.put(Dict.RET_CODE, exceptionInfo.getCode());
        fateMap.put(Dict.RET_MSG, exceptionInfo.getMessage());
        System.err.println("kaideng  test ppppppppppppppppppp"+ fateMap);
        builder.setBody(dataBuilder.setValue(ByteString.copyFromUtf8(JsonUtil.object2Json(fateMap))));
        return builder.build();
    }

}

