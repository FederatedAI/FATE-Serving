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

package com.webank.ai.fate.serving.proxy.test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Preconditions;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceProto;
import com.webank.ai.fate.api.networking.common.CommonServiceProto;
import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.serving.common.bean.BaseContext;
import com.webank.ai.fate.serving.common.bean.ServingServerContext;
import com.webank.ai.fate.serving.common.flow.MetricNode;
import com.webank.ai.fate.serving.common.model.Model;
import com.webank.ai.fate.serving.common.rpc.core.FederatedRpcInvoker;
import com.webank.ai.fate.serving.common.utils.HttpClientPool;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

import java.util.List;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CommonServiceTest {

    static {
        HttpClientPool.initPool();
    }

    @BeforeClass
    public static void init() {

    }


    private Proxy.Packet buildPacket(Context context, FederatedRpcInvoker.RpcDataWraper rpcDataWraper) {
        Model model = ((ServingServerContext) context).getModel();
        Preconditions.checkArgument(model != null);
        Proxy.Packet.Builder packetBuilder = Proxy.Packet.newBuilder();
        packetBuilder.setBody(Proxy.Data.newBuilder().setValue(ByteString.copyFrom(JsonUtil.object2Json(rpcDataWraper.getData()).getBytes())).build());
        Proxy.Metadata.Builder metaDataBuilder = Proxy.Metadata.newBuilder();
        Proxy.Topic.Builder topicBuilder = Proxy.Topic.newBuilder();
        metaDataBuilder.setSrc(topicBuilder.setPartyId(String.valueOf(model.getPartId())).setRole(MetaInfo.PROPERTY_SERVICE_ROLE_NAME).setName(Dict.PARTNER_PARTY_NAME).build());
        metaDataBuilder.setDst(topicBuilder.setPartyId(String.valueOf(rpcDataWraper.getHostModel().getPartId())).setRole(MetaInfo.PROPERTY_SERVICE_ROLE_NAME).setName(Dict.PARTY_NAME).build());
        metaDataBuilder.setCommand(Proxy.Command.newBuilder().setName(rpcDataWraper.getRemoteMethodName()).build());
        String version = Long.toString(MetaInfo.CURRENT_VERSION);
        metaDataBuilder.setOperator(version);
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


    @Test
    public void test_http_unary() {
        String url = "http://localhost:8059/uncary";
        Model guestModel = new Model();
        Model hostModel = new Model();
        guestModel.setPartId("9999");
        hostModel.setPartId("10000");
        hostModel.setNamespace("testNamespace");
        hostModel.setTableName("testTablename");
        MetaInfo.PROPERTY_SERVICE_ROLE_NAME="serving";

        ServingServerContext context = new ServingServerContext();
        context.setModel(guestModel);
        FederatedRpcInvoker.RpcDataWraper   rpcDataWraper = new  FederatedRpcInvoker.RpcDataWraper();
        rpcDataWraper.setRemoteMethodName("uncaryCall");
        rpcDataWraper.setData("my name is test");
        rpcDataWraper.setHostModel(hostModel);
        Proxy.Packet packet =buildPacket(context,rpcDataWraper);

        try {
          String  content=   JsonFormat.printer().print(packet);
          System.err.println("================"+content);
          String  result = HttpClientPool.sendPost(url,content,null);
          System.err.println("==============result:"+result);
          Proxy.Packet.Builder  resultBuilder =   Proxy.Packet.newBuilder();
          JsonFormat.parser().merge(result,resultBuilder);
            Proxy.Packet resultPacket =  resultBuilder.build();
            System.err.println(resultPacket.getBody().getValue().toStringUtf8());

        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }


    }


}