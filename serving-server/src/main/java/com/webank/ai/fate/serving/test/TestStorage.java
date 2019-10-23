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

package com.webank.ai.fate.serving.test;

import com.google.common.collect.Maps;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceGrpc;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceProto;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TestStorage {
    public static void main(String[] args) {
//        ManagedChannel channel = ManagedChannelBuilder.forAddress("127.0.0.1", 8011).usePlaintext().build();
//        KVServiceGrpc.KVServiceBlockingStub kvServiceBlockingStub = KVServiceGrpc.newBlockingStub(channel);
//
//        StorageBasic.StorageLocator.Builder storageLocator = StorageBasic.StorageLocator.newBuilder();
//        storageLocator.setType(StorageBasic.StorageType.LMDB)
//                .setNamespace("jarvis_test")
//                .setName("jarvis_test2")
//                .setFragment(0);
//
//        Kv.CreateTableInfo.Builder tableInfo = Kv.CreateTableInfo.newBuilder();
//        tableInfo.setStorageLocator(storageLocator.build()).setFragmentCount(1);
//        System.out.println(tableInfo.build());
//
//        Kv.CreateTableInfo newTableInfo = kvServiceBlockingStub.createIfAbsent(tableInfo.build());
//        System.out.println(newTableInfo);
//
//        Kv.Operand.Builder requestOperand = Kv.Operand.newBuilder();
//        requestOperand.setKey(ByteString.copyFrom("k1".getBytes()));
//        requestOperand.setValue(ByteString.copyFrom("v2".getBytes()));
//        Metadata header = new Metadata();
//        header.put(CompositeHeaderKey.from("table_name").asMetaKey(), "jarvis_test4");
//        header.put(CompositeHeaderKey.from("name_space").asMetaKey(), "jarvis_test");
//        header.put(CompositeHeaderKey.from("store_type").asMetaKey(), "LMDB");
//        Kv.Operand resultOperand = MetadataUtils.attachHeaders(kvServiceBlockingStub, header).get(requestOperand.build());
//        System.out.println(StringUtils.isEmpty(resultOperand.getValue().toStringUtf8()));
//        System.out.println(resultOperand.getValue().toStringUtf8());

        //        message PublishRequest{
//            LocalInfo local = 1;
//            map<string, Party> role = 2;
//            map<string, RoleModelInfo> model = 3;
//        }


        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8000).usePlaintext().build();

        ModelServiceGrpc.ModelServiceBlockingStub blockingStub = ModelServiceGrpc.newBlockingStub(channel);

        blockingStub = blockingStub.withDeadlineAfter(3000, TimeUnit.MILLISECONDS);

        ModelServiceProto.PublishRequest.Builder publishRequestBuilder = ModelServiceProto.PublishRequest.newBuilder();


        Map<String, ModelServiceProto.Party> role = Maps.newHashMap();
        role.put("3333333", ModelServiceProto.Party.newBuilder().addPartyId("10001").build());

        Map<String, ModelServiceProto.RoleModelInfo> models = Maps.newHashMap();
        Map<String, ModelServiceProto.ModelInfo> modelInfoMap = Maps.newHashMap();

        modelInfoMap.put("555555", ModelServiceProto.ModelInfo.newBuilder().setNamespace("mynamespace").setTableName("mytablename").build());

        models.put("4444444444", ModelServiceProto.RoleModelInfo.newBuilder().putAllRoleModelInfo(modelInfoMap).build());
        ModelServiceProto.LocalInfo localInfo = ModelServiceProto.LocalInfo.newBuilder().setPartyId("10001").setRole("Guest").build();
        publishRequestBuilder.setLocal(localInfo);
        publishRequestBuilder.putAllRole(role);
        publishRequestBuilder.putAllModel(models);

        publishRequestBuilder.build();


        System.err.println(blockingStub.publishLoad(publishRequestBuilder.build()));

    }
}
