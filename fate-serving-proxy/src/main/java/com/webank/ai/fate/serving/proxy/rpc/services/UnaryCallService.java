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
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.webank.ai.fate.api.networking.proxy.DataTransferServiceGrpc;
import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.serving.common.rpc.core.AbstractServiceAdaptor;
import com.webank.ai.fate.serving.common.rpc.core.FateService;
import com.webank.ai.fate.serving.common.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.common.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.common.utils.HttpClientPool;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.exceptions.*;
import com.webank.ai.fate.serving.core.rpc.router.Protocol;
import com.webank.ai.fate.serving.core.rpc.router.RouterInfo;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import com.webank.ai.fate.serving.proxy.security.AuthUtils;
import io.grpc.ManagedChannel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    private final int timeout = MetaInfo.PROPERTY_PROXY_GRPC_UNARYCALL_TIMEOUT;

    @Override
    public Proxy.Packet doService(Context context, InboundPackage<Proxy.Packet> data, OutboundPackage<Proxy.Packet> outboundPackage) {
        Proxy.Packet sourcePackage = data.getBody();
        RouterInfo routerInfo = data.getRouterInfo();
        try {
            sourcePackage = authUtils.addAuthInfo(sourcePackage);
        } catch (Exception e) {
            logger.error("add auth info error", e);
            throw new ProxyAuthException("add auth info error");
        }
        if(routerInfo.getProtocol()==null||routerInfo.getProtocol().equals(Protocol.GRPC)){
            return grpcTransfer(context,sourcePackage,routerInfo);
        }else if(routerInfo.getProtocol().equals(Protocol.HTTP)){
            return httpTransfer(context,sourcePackage,routerInfo);
        }else{
            throw new  RemoteRpcException("");
        }
    }

    private Proxy.Packet grpcTransfer(Context context,Proxy.Packet sourcePackage,RouterInfo  routerInfo){

        try {
            NettyServerInfo nettyServerInfo;
            if (routerInfo.isUseSSL()) {
                nettyServerInfo = new NettyServerInfo(routerInfo.getNegotiationType(), routerInfo.getCertChainFile(),
                        routerInfo.getPrivateKeyFile(), routerInfo.getCaFile());
            } else {
                nettyServerInfo = new NettyServerInfo();
            }

            ManagedChannel managedChannel = grpcConnectionPool.getManagedChannel(routerInfo.getHost(), routerInfo.getPort(), nettyServerInfo);
            DataTransferServiceGrpc.DataTransferServiceFutureStub stub1 = DataTransferServiceGrpc.newFutureStub(managedChannel);
            stub1.withDeadlineAfter(timeout, TimeUnit.MILLISECONDS);
            context.setDownstreamBegin(System.currentTimeMillis());
            ListenableFuture<Proxy.Packet> future = stub1.unaryCall(sourcePackage);
            Proxy.Packet packet = future.get(timeout, TimeUnit.MILLISECONDS);
            return packet;

        } catch (Exception e) {
            logger.error("unaryCall error", e);
            throw new RemoteRpcException("grpc unaryCall error " + routerInfo.toString());
        } finally {
            long end = System.currentTimeMillis();
            context.setDownstreamCost(end - context.getDownstreamBegin());
        }
    }

    private Proxy.Packet httpTransfer(Context context,Proxy.Packet sourcePackage,RouterInfo  routerInfo) throws BaseException{
        try {
            String url = routerInfo.getUrl();
            String content = null;
            content = JsonFormat.printer().print(sourcePackage);
            String resultJson = HttpClientPool.sendPost(url, content, null);
            logger.info("result json {}", resultJson);
            Proxy.Packet.Builder resultBuilder = Proxy.Packet.newBuilder();
            try {
                JsonFormat.parser().merge(resultJson, resultBuilder);
            }catch(Exception  e){
                logger.error("receive invalid http response {}",resultJson);
                throw  new InvalidResponseException("receive invalid http response");
            }
            return resultBuilder.build();
        }catch(Exception e){
            if(e instanceof BaseException){
                throw (BaseException) e;
            }else{
                throw new SysException(e.getMessage());
            }
        }
    }

    @Override
    protected Proxy.Packet transformExceptionInfo(Context context, ExceptionInfo exceptionInfo) {
        Proxy.Packet.Builder builder = Proxy.Packet.newBuilder();
        Proxy.Data.Builder dataBuilder = Proxy.Data.newBuilder();
        Map fateMap = Maps.newHashMap();
        fateMap.put(Dict.RET_CODE, exceptionInfo.getCode()+500);
        fateMap.put(Dict.RET_MSG, exceptionInfo.getMessage());
        builder.setBody(dataBuilder.setValue(ByteString.copyFromUtf8(JsonUtil.object2Json(fateMap))));
        return builder.build();
    }
}

