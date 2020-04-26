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
import com.webank.ai.fate.register.router.RouterService;
import com.webank.ai.fate.register.url.URL;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.GrpcConnectionPool;
import com.webank.ai.fate.serving.core.bean.ServingServerContext;
import com.webank.ai.fate.serving.core.model.Model;
import com.webank.ai.fate.serving.core.rpc.core.FederatedRpcInvoker;
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
public class DefaultFederatedRpcInvoker implements FederatedRpcInvoker<Proxy.Packet> {

    private static final Logger logger = LoggerFactory.getLogger(FederatedRpcInvoker.class);
    @Autowired(required = false)
    public RouterService routerService;
    @Autowired
    private Environment environment;

    private Proxy.Packet build(Context context, RpcDataWraper rpcDataWraper) {

        Model model = ((ServingServerContext) context).getModel();
        Preconditions.checkArgument(model != null);
        Proxy.Packet.Builder packetBuilder = Proxy.Packet.newBuilder();
        packetBuilder.setBody(Proxy.Data.newBuilder()
                .setValue(ByteString.copyFrom(JSON.toJSONBytes(rpcDataWraper.getData())))
                .build());

        Proxy.Metadata.Builder metaDataBuilder = Proxy.Metadata.newBuilder();
        Proxy.Topic.Builder topicBuilder = Proxy.Topic.newBuilder();

//        Preconditions.checkArgument(StringUtils.isNotEmpty(context.getGuestAppId()));
//        Preconditions.checkArgument(StringUtils.isNotEmpty(context.getHostAppid()));

        metaDataBuilder.setSrc(
                topicBuilder.setPartyId(String.valueOf(model.getPartId())).
                        setRole(environment.getProperty(Dict.PROPERTY_SERVICE_ROLE_NAME, Dict.PROPERTY_SERVICE_ROLE_NAME_DEFAULT_VALUE))
                        .setName(Dict.PARTNER_PARTY_NAME)
                        .build());
        metaDataBuilder.setDst(
                topicBuilder.setPartyId(String.valueOf(rpcDataWraper.getHostModel().getPartId()))
                        .setRole(environment.getProperty(Dict.PROPERTY_SERVICE_ROLE_NAME, Dict.PROPERTY_SERVICE_ROLE_NAME_DEFAULT_VALUE))
                        .setName(Dict.PARTY_NAME)
                        .build());
        metaDataBuilder.setCommand(Proxy.Command.newBuilder().setName(rpcDataWraper.getRemoteMethodName()).build());
        // TODO: 2020/3/23   这里记得加入版本号
        String version = environment.getProperty(Dict.VERSION, "");
        metaDataBuilder.setOperator(environment.getProperty(Dict.VERSION, ""));
        Proxy.Task.Builder taskBuilder = com.webank.ai.fate.api.networking.proxy.Proxy.Task.newBuilder();
        Proxy.Model.Builder modelBuilder = Proxy.Model.newBuilder();

        modelBuilder.setNamespace(rpcDataWraper.getHostModel().getNamespace());
        modelBuilder.setTableName(rpcDataWraper.getHostModel().getTableName());
        taskBuilder.setModel(modelBuilder.build());

        metaDataBuilder.setTask(taskBuilder.build());
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

        return packetBuilder.build();

    }

    private String getVersion() {

        return "";
    }

    private String route() {

        boolean routerByzk = environment.getProperty(Dict.USE_ZK_ROUTER, boolean.class, Boolean.TRUE);
        String address = null;
        if (!routerByzk) {
            address = environment.getProperty(Dict.PROPERTY_PROXY_ADDRESS);
        } else {
//            URL paramUrl = URL.valueOf(Dict.PROPERTY_PROXY_ADDRESS + "/" + Dict.ONLINE_ENVIROMMENT + "/" + Dict.UNARYCALL);
//            URL newUrl = paramUrl.addParameter(Constants.VERSION_KEY, this.getVersion());

            List<URL> urls = routerService.router(Dict.PROPERTY_PROXY_ADDRESS, Dict.ONLINE_ENVIROMMENT, Dict.UNARYCALL);
            if (urls != null && urls.size() > 0) {
                URL url = urls.get(0);
                String ip = url.getHost();
                int port = url.getPort();
                address = ip + ":" + port;
            }
        }
        return address;
    }


    @Override
    public Proxy.Packet sync(Context context, RpcDataWraper rpcDataWraper, long timeout) {


        Proxy.Packet resultPacket = null;
        try {

            ListenableFuture<Proxy.Packet> future = this.async(context, rpcDataWraper);

            if (future != null) {
                resultPacket = future.get(timeout, TimeUnit.MILLISECONDS);
            }
            return resultPacket;

        } catch (Exception e) {
            logger.error("getFederatedPredictFromRemote error", e.getMessage());
            throw new RuntimeException(e);
        } finally {
            context.setDownstreamCost(System.currentTimeMillis() - context.getDownstreamBegin());
        }

    }


    @Override
    public ListenableFuture<Proxy.Packet> async(Context context, RpcDataWraper rpcDataWraper) {

        context.setDownstreamBegin(System.currentTimeMillis());

        try {

            Proxy.Packet packet = this.build(context, rpcDataWraper);

            Proxy.Packet resultPacket = null;

            String address = this.route();

            GrpcConnectionPool grpcConnectionPool = GrpcConnectionPool.getPool();

            Preconditions.checkArgument(StringUtils.isNotEmpty(address));

            logger.info("try to send to {}", address);

            ManagedChannel channel1 = grpcConnectionPool.getManagedChannel(address);

            ListenableFuture<Proxy.Packet> future = null;

            //DataTransferServiceGrpc.DataTransferServiceBlockingStub stub1 = DataTransferServiceGrpc.newBlockingStub(channel1);
            DataTransferServiceGrpc.DataTransferServiceFutureStub stub1 = DataTransferServiceGrpc.newFutureStub(channel1);

            future = stub1.unaryCall(packet);

            return future;

        } catch (Exception e) {
            logger.error("getFederatedPredictFromRemote error", e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }


}
