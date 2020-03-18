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

package com.webank.ai.fate.serving.rpc;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.ByteString;
import com.webank.ai.fate.api.networking.proxy.DataTransferServiceGrpc;
import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.register.common.Constants;
import com.webank.ai.fate.register.router.RouterService;
import com.webank.ai.fate.register.url.URL;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.rpc.router.RouterInfo;
import com.webank.ai.fate.serving.core.utils.ObjectTransform;
import io.grpc.ManagedChannel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class FederatedRpcInvoker {

    @Autowired
    public RouterService routerService;

    @Autowired
    private Environment environment;

    private static final Logger logger = LoggerFactory.getLogger(FederatedRpcInvoker.class);

    public ReturnResult sync(Context context, FederatedParty srcParty, FederatedParty dstParty, HostFederatedParams hostFederatedParams, String remoteMethodName) {
        long beginTime = System.currentTimeMillis();
        ReturnResult remoteResult = null;
        try {

            Proxy.Packet.Builder packetBuilder = Proxy.Packet.newBuilder();
            packetBuilder.setBody(Proxy.Data.newBuilder()
                    .setValue(ByteString.copyFrom(JSON.toJSONBytes(hostFederatedParams)))
                    .build());

            Proxy.Metadata.Builder metaDataBuilder = Proxy.Metadata.newBuilder();
            Proxy.Topic.Builder topicBuilder = Proxy.Topic.newBuilder();

            metaDataBuilder.setSrc(
                    topicBuilder.setPartyId(String.valueOf(srcParty.getPartyId())).
                            setRole(environment.getProperty(Dict.PROPERTY_SERVICE_ROLE_NAME, Dict.PROPERTY_SERVICE_ROLE_NAME_DEFAULT_VALUE))
                            .setName(Dict.PARTNER_PARTY_NAME)
                            .build());
            metaDataBuilder.setDst(
                    topicBuilder.setPartyId(String.valueOf(dstParty.getPartyId()))
                            .setRole(environment.getProperty(Dict.PROPERTY_SERVICE_ROLE_NAME, Dict.PROPERTY_SERVICE_ROLE_NAME_DEFAULT_VALUE))
                            .setName(Dict.PARTY_NAME)
                            .build());
            metaDataBuilder.setCommand(Proxy.Command.newBuilder().setName(remoteMethodName).build());
            metaDataBuilder.setConf(Proxy.Conf.newBuilder().setOverallTimeout(60 * 1000));
            String version = environment.getProperty(Dict.VERSION, "");
            metaDataBuilder.setOperator(environment.getProperty(Dict.VERSION, ""));
            packetBuilder.setHeader(metaDataBuilder.build());
            Proxy.AuthInfo.Builder authBuilder = Proxy.AuthInfo.newBuilder();
            if (context.getCaseId() != null) {
                authBuilder.setNonce(context.getCaseId());
            }
            if (version != null) {
                authBuilder.setVersion(version);
            }
            if (context.getServiceId() != null) {
                authBuilder.setServiceId(context.getServiceId());
            }
            if (context.getApplyId() != null) {
                authBuilder.setApplyId(context.getApplyId());
            }
            packetBuilder.setAuth(authBuilder.build());

            GrpcConnectionPool grpcConnectionPool = GrpcConnectionPool.getPool();
            boolean routerByzk = environment.getProperty(Dict.USE_ZK_ROUTER, boolean.class, Boolean.TRUE);
            String address = null;
            if (!routerByzk) {
                address = environment.getProperty(Dict.PROPERTY_PROXY_ADDRESS);
            } else {
                URL paramUrl = URL.valueOf(Dict.PROPERTY_PROXY_ADDRESS + "/" + Dict.ONLINE_ENVIROMMENT + "/" + Dict.UNARYCALL);
                URL newUrl = paramUrl.addParameter(Constants.VERSION_KEY, version);
                List<URL> urls = routerService.router(newUrl);
                if (urls != null && urls.size() > 0) {
                    URL url = urls.get(0);
                    String ip = url.getHost();
                    int port = url.getPort();
                    address = ip + ":" + port;
                }
            }
            Preconditions.checkArgument(StringUtils.isNotEmpty(address));
            ManagedChannel channel1 = grpcConnectionPool.getManagedChannel(address);
            ListenableFuture<Proxy.Packet> future = null;

            //DataTransferServiceGrpc.DataTransferServiceBlockingStub stub1 = DataTransferServiceGrpc.newBlockingStub(channel1);
            DataTransferServiceGrpc.DataTransferServiceFutureStub stub1 = DataTransferServiceGrpc.newFutureStub(channel1);
            future = stub1.unaryCall(packetBuilder.build());

            if (future != null) {
                Proxy.Packet packet = future.get(environment.getProperty("rpc.time.out", int.class, 3000), TimeUnit.MILLISECONDS);
                remoteResult = (ReturnResult) ObjectTransform.json2Bean(packet.getBody().getValue().toStringUtf8(), ReturnResult.class);
            }
            return remoteResult;
        } catch (Exception e) {
            logger.error("getFederatedPredictFromRemote error", e.getMessage());
            throw new RuntimeException(e);
        } finally {
            long end = System.currentTimeMillis();
            long cost = end - beginTime;
            logger.info("caseid {} getFederatedPredictFromRemote cost {} remote retcode {}", context.getCaseId(), cost, remoteResult != null ? remoteResult.getRetcode() : Dict.NONE);
        }

    }

    public ListenableFuture<Proxy.Packet> asyncBatch(Context context, BatchHostFederatedParams batchHostFederatedParams) {
        ReturnResult remoteResult = null;
        try {

            Proxy.Packet.Builder packetBuilder = Proxy.Packet.newBuilder();
            packetBuilder.setBody(Proxy.Data.newBuilder()
                    .setValue(ByteString.copyFrom(JSON.toJSONBytes(batchHostFederatedParams)))
                    .build());
            Proxy.Metadata.Builder metaDataBuilder = Proxy.Metadata.newBuilder();
            Proxy.Topic.Builder topicBuilder = Proxy.Topic.newBuilder();

            metaDataBuilder.setSrc(
                    topicBuilder.setPartyId(String.valueOf(batchHostFederatedParams.getGuestPartyId())).
                            setRole(environment.getProperty(Dict.PROPERTY_SERVICE_ROLE_NAME, Dict.PROPERTY_SERVICE_ROLE_NAME_DEFAULT_VALUE))
                            .setName(Dict.PARTNER_PARTY_NAME)
                            .build());
            metaDataBuilder.setDst(
                    topicBuilder.setPartyId(String.valueOf(batchHostFederatedParams.getHostPartyId()))
                            .setRole(environment.getProperty(Dict.PROPERTY_SERVICE_ROLE_NAME, Dict.PROPERTY_SERVICE_ROLE_NAME_DEFAULT_VALUE))
                            .setName(Dict.PARTY_NAME)
                            .build());
            metaDataBuilder.setCommand(Proxy.Command.newBuilder().setName("batch").build());
            metaDataBuilder.setConf(Proxy.Conf.newBuilder().setOverallTimeout(60 * 1000));
            String version = environment.getProperty(Dict.VERSION, "");
            metaDataBuilder.setOperator(environment.getProperty(Dict.VERSION, ""));
            packetBuilder.setHeader(metaDataBuilder.build());
            Proxy.AuthInfo.Builder authBuilder = Proxy.AuthInfo.newBuilder();
            if (context.getCaseId() != null) {
                authBuilder.setNonce(context.getCaseId());
            }
            if (version != null) {
                authBuilder.setVersion(version);
            }
            if (context.getServiceId() != null) {
                authBuilder.setServiceId(context.getServiceId());
            }
            if (context.getApplyId() != null) {
                authBuilder.setApplyId(context.getApplyId());
            }
            packetBuilder.setAuth(authBuilder.build());

            /**
             *  在之前的组件获取ip
             */
            RouterInfo routerInfo = context.getRouterInfo();

            Preconditions.checkArgument(routerInfo != null);

            GrpcConnectionPool grpcConnectionPool = GrpcConnectionPool.getPool();
//            String routerByZkString = Configuration.getProperty(Dict.USE_ZK_ROUTER, "true");
//            boolean routerByzk = Boolean.valueOf(routerByZkString);
//            String address = null;
//            if (!routerByzk) {
//                address = Configuration.getProperty(Dict.PROPERTY_PROXY_ADDRESS);
//            } else {
//                URL paramUrl = URL.valueOf(Dict.PROPERTY_PROXY_ADDRESS + "/" + Dict.ONLINE_ENVIROMMENT + "/" + Dict.UNARYCALL);
//                URL newUrl =paramUrl.addParameter(Constants.VERSION_KEY,version);
//                List<URL> urls = routerService.router(newUrl);
//                if (urls!=null&&urls.size() > 0) {
//                    URL url = urls.get(0);
//                    String ip = url.getHost();
//                    int port = url.getPort();
//                    address = ip + ":" + port;
//                }
//            }

            ManagedChannel channel1 = grpcConnectionPool.getManagedChannel(routerInfo.toString());
            ListenableFuture<Proxy.Packet> future = null;

            //DataTransferServiceGrpc.DataTransferServiceBlockingStub stub1 = DataTransferServiceGrpc.newBlockingStub(channel1);
            DataTransferServiceGrpc.DataTransferServiceFutureStub stub1 = DataTransferServiceGrpc.newFutureStub(channel1);
            future = stub1.unaryCall(packetBuilder.build());

//            if(future!=null){
//                Proxy.Packet packet = future.get(Configuration.getPropertyInt("rpc.time.out",3000), TimeUnit.MILLISECONDS);
//                remoteResult = (ReturnResult) ObjectTransform.json2Bean(packet.getBody().getValue().toStringUtf8(), ReturnResult.class);
//            }
//            return remoteResult;
            return future;

        } catch (Exception e) {
            logger.error("getFederatedPredictFromRemote error", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public ListenableFuture<Proxy.Packet> async(Context context, FederatedParty srcParty, FederatedParty dstParty, HostFederatedParams hostFederatedParams, String remoteMethodName) {
        long beginTime = System.currentTimeMillis();
        ReturnResult remoteResult = null;
        try {

            Proxy.Packet.Builder packetBuilder = Proxy.Packet.newBuilder();
            packetBuilder.setBody(Proxy.Data.newBuilder()
                    .setValue(ByteString.copyFrom(JSON.toJSONBytes(hostFederatedParams)))
                    .build());

            Proxy.Metadata.Builder metaDataBuilder = Proxy.Metadata.newBuilder();
            Proxy.Topic.Builder topicBuilder = Proxy.Topic.newBuilder();

            metaDataBuilder.setSrc(
                    topicBuilder.setPartyId(String.valueOf(srcParty.getPartyId())).
                            setRole(environment.getProperty(Dict.PROPERTY_SERVICE_ROLE_NAME, Dict.PROPERTY_SERVICE_ROLE_NAME_DEFAULT_VALUE))
                            .setName(Dict.PARTNER_PARTY_NAME)
                            .build());
            metaDataBuilder.setDst(
                    topicBuilder.setPartyId(String.valueOf(dstParty.getPartyId()))
                            .setRole(environment.getProperty(Dict.PROPERTY_SERVICE_ROLE_NAME, Dict.PROPERTY_SERVICE_ROLE_NAME_DEFAULT_VALUE))
                            .setName(Dict.PARTY_NAME)
                            .build());
            metaDataBuilder.setCommand(Proxy.Command.newBuilder().setName(remoteMethodName).build());
            metaDataBuilder.setConf(Proxy.Conf.newBuilder().setOverallTimeout(60 * 1000));
            String version = environment.getProperty(Dict.VERSION, "");
            metaDataBuilder.setOperator(environment.getProperty(Dict.VERSION, ""));
            packetBuilder.setHeader(metaDataBuilder.build());
            Proxy.AuthInfo.Builder authBuilder = Proxy.AuthInfo.newBuilder();
            if (context.getCaseId() != null) {
                authBuilder.setNonce(context.getCaseId());
            }
            if (version != null) {
                authBuilder.setVersion(version);
            }
            if (context.getServiceId() != null) {
                authBuilder.setServiceId(context.getServiceId());
            }
            if (context.getApplyId() != null) {
                authBuilder.setApplyId(context.getApplyId());
            }
            packetBuilder.setAuth(authBuilder.build());

            GrpcConnectionPool grpcConnectionPool = GrpcConnectionPool.getPool();
            boolean routerByzk = environment.getProperty(Dict.USE_ZK_ROUTER, boolean.class, Boolean.TRUE);
            String address = null;
            if (!routerByzk) {
                address = environment.getProperty(Dict.PROPERTY_PROXY_ADDRESS);
            } else {

                URL paramUrl = URL.valueOf(Dict.PROPERTY_PROXY_ADDRESS + "/" + Dict.ONLINE_ENVIROMMENT + "/" + Dict.UNARYCALL);
                URL newUrl = paramUrl.addParameter(Constants.VERSION_KEY, version);
                List<URL> urls = routerService.router(newUrl);
                if (urls != null && urls.size() > 0) {
                    URL url = urls.get(0);
                    String ip = url.getHost();
                    int port = url.getPort();
                    address = ip + ":" + port;
                }
            }
            Preconditions.checkArgument(StringUtils.isNotEmpty(address));
            ManagedChannel channel1 = grpcConnectionPool.getManagedChannel(address);
            ListenableFuture<Proxy.Packet> future = null;

            //DataTransferServiceGrpc.DataTransferServiceBlockingStub stub1 = DataTransferServiceGrpc.newBlockingStub(channel1);
            DataTransferServiceGrpc.DataTransferServiceFutureStub stub1 = DataTransferServiceGrpc.newFutureStub(channel1);
            future = stub1.unaryCall(packetBuilder.build());

//            if(future!=null){
//                Proxy.Packet packet = future.get(Configuration.getPropertyInt("rpc.time.out",3000), TimeUnit.MILLISECONDS);
//                remoteResult = (ReturnResult) ObjectTransform.json2Bean(packet.getBody().getValue().toStringUtf8(), ReturnResult.class);
//            }
//            return remoteResult;
            return future;

        } catch (Exception e) {
            logger.error("getFederatedPredictFromRemote error", e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
