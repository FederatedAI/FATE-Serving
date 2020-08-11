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

package com.webank.ai.fate.serving.sdk.client;

import com.google.common.base.Preconditions;
import com.google.protobuf.ByteString;
import com.webank.ai.fate.api.serving.InferenceServiceGrpc;
import com.webank.ai.fate.api.serving.InferenceServiceProto;
import com.webank.ai.fate.register.router.DefaultRouterService;
import com.webank.ai.fate.register.router.RouterService;
import com.webank.ai.fate.register.url.URL;
import com.webank.ai.fate.register.zookeeper.ZookeeperRegistry;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import io.grpc.ManagedChannel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class Client {

    static final String PROJECT = "serving";
    static final String SINGLE_INFERENCE = "inference";
    static final String BATCH_INFERENCE = "batchInference";
    static final String ClIENT_PROJECT = "client";
    static final int port = 36578;
    static final String ENVIRONMENT = "online";
    private RouterService routerService;
    private GrpcConnectionPool grpcConnectionPool = GrpcConnectionPool.getPool();
    private static Map<String, Client> clientMap = new ConcurrentHashMap<>();

    public static synchronized Client getClient() {
        return new Client();
    }

    public static synchronized Client getClient(String zkAddress) {
        MetaInfo.PROPERTY_ACL_ENABLE = false;
        if (clientMap.get(zkAddress) == null) {
            Client client = new Client();
            client.setRegistryAddress(zkAddress);
            clientMap.put(zkAddress, client);
        }
        return clientMap.get(zkAddress);
    }

    public Client setRegistryAddress(String address) {
        Preconditions.checkArgument(StringUtils.isNotBlank(address), "address is blank");
        ZookeeperRegistry zookeeperRegistry = ZookeeperRegistry.createRegistry(address, ClIENT_PROJECT, ENVIRONMENT, port);
        zookeeperRegistry.subProject(PROJECT);
        DefaultRouterService defaultRouterService = new DefaultRouterService();
        defaultRouterService.setRegistry(zookeeperRegistry);
        this.routerService = defaultRouterService;
        return this;
    }

    public static Client getClient(String zkAddress, boolean useAcl, String aclUserName, String aclPassword) {
        if (useAcl) {
            System.setProperty("acl.enable", "true");
        } else {
            System.setProperty("acl.enable", "false");
        }
        System.setProperty("acl.username", aclUserName != null ? aclUserName : "");
        System.setProperty("acl.password", aclPassword != null ? aclPassword : "");
        MetaInfo.PROPERTY_ACL_ENABLE = useAcl;
        return getClient(zkAddress);
    }

    public ReturnResult singleInference(InferenceRequest inferenceRequest) throws Exception {
        Preconditions.checkArgument(inferenceRequest != null, "inferenceRequest is null");
        Preconditions.checkArgument(this.routerService != null, "router service is null, please call setRegistryAddress(String address) to config registry");
        Preconditions.checkArgument(StringUtils.isNotEmpty(inferenceRequest.getServiceId()));
        List<URL> urls = routerService.router(PROJECT, inferenceRequest.getServiceId(), SINGLE_INFERENCE);
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(urls), "serviceId " + inferenceRequest.getServiceId() + " found no url in zk");
        URL url = urls.get(0);

        InferenceServiceProto.InferenceMessage.Builder inferenceMessageBuilder = InferenceServiceProto.InferenceMessage.newBuilder();
        inferenceMessageBuilder.setBody(ByteString.copyFrom(JsonUtil.object2Json(inferenceRequest), "UTF-8"));
        InferenceServiceProto.InferenceMessage inferenceMessage = inferenceMessageBuilder.build();
        InferenceServiceGrpc.InferenceServiceBlockingStub blockingStub = this.getInferenceServiceBlockingStub(url.getHost(), url.getPort());
        InferenceServiceProto.InferenceMessage result = blockingStub.inference(inferenceMessage);
        Preconditions.checkArgument(result != null && result.getBody() != null);
        return JsonUtil.json2Object(result.getBody().toByteArray(), ReturnResult.class);
    }

    public ReturnResult singleInference(String host, int port, InferenceRequest inferenceRequest) throws Exception {
        Preconditions.checkArgument(inferenceRequest != null, "inferenceRequest is null");
        Preconditions.checkArgument(StringUtils.isNotEmpty(inferenceRequest.getServiceId()));

        InferenceServiceProto.InferenceMessage.Builder inferenceMessageBuilder = InferenceServiceProto.InferenceMessage.newBuilder();
        inferenceMessageBuilder.setBody(ByteString.copyFrom(JsonUtil.object2Json(inferenceRequest), "UTF-8"));
        InferenceServiceProto.InferenceMessage inferenceMessage = inferenceMessageBuilder.build();
        InferenceServiceGrpc.InferenceServiceBlockingStub blockingStub = this.getInferenceServiceBlockingStub(host, port);
        InferenceServiceProto.InferenceMessage result = blockingStub.inference(inferenceMessage);
        Preconditions.checkArgument(result != null && result.getBody() != null);
        return JsonUtil.json2Object(result.getBody().toByteArray(), ReturnResult.class);
    }

    public BatchInferenceResult batchInference(BatchInferenceRequest batchInferenceRequest) throws Exception {
        Preconditions.checkArgument(batchInferenceRequest != null, "batchInferenceRequest is null");
        Preconditions.checkArgument(this.routerService != null, "router service is null, please call setRegistryAddress(String address) to config registry");
        Preconditions.checkArgument(StringUtils.isNotEmpty(batchInferenceRequest.getServiceId()));
        List<URL> urls = routerService.router(PROJECT, batchInferenceRequest.getServiceId(), BATCH_INFERENCE);
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(urls), "serviceId " + batchInferenceRequest.getServiceId() + " found no url in zk");
        URL url = urls.get(0);

        InferenceServiceProto.InferenceMessage.Builder inferenceMessageBuilder = InferenceServiceProto.InferenceMessage.newBuilder();
        inferenceMessageBuilder.setBody(ByteString.copyFrom(JsonUtil.object2Json(batchInferenceRequest), "UTF-8"));
        InferenceServiceGrpc.InferenceServiceBlockingStub blockingStub = this.getInferenceServiceBlockingStub(url.getHost(), url.getPort());
        InferenceServiceProto.InferenceMessage result = blockingStub.batchInference(inferenceMessageBuilder.build());
        Preconditions.checkArgument(result != null && result.getBody() != null);
        return JsonUtil.json2Object(result.getBody().toByteArray(), BatchInferenceResult.class);
    }

    public BatchInferenceResult batchInference(String host, int port, BatchInferenceRequest batchInferenceRequest) throws Exception {
        Preconditions.checkArgument(batchInferenceRequest != null, "batchInferenceRequest is null");
        Preconditions.checkArgument(StringUtils.isNotEmpty(batchInferenceRequest.getServiceId()));

        InferenceServiceProto.InferenceMessage.Builder inferenceMessageBuilder = InferenceServiceProto.InferenceMessage.newBuilder();
        inferenceMessageBuilder.setBody(ByteString.copyFrom(JsonUtil.object2Json(batchInferenceRequest), "UTF-8"));
        InferenceServiceGrpc.InferenceServiceBlockingStub blockingStub = this.getInferenceServiceBlockingStub(host, port);
        InferenceServiceProto.InferenceMessage result = blockingStub.batchInference(inferenceMessageBuilder.build());
        Preconditions.checkArgument(result != null && result.getBody() != null);
        return JsonUtil.json2Object(result.getBody().toByteArray(), BatchInferenceResult.class);
    }

    private InferenceServiceGrpc.InferenceServiceBlockingStub getInferenceServiceBlockingStub(String host, int port) throws Exception {
        Preconditions.checkArgument(StringUtils.isNotBlank(host), "host is blank");
        ManagedChannel managedChannel = grpcConnectionPool.getManagedChannel(host, port);
        InferenceServiceGrpc.InferenceServiceBlockingStub blockingStub = InferenceServiceGrpc.newBlockingStub(managedChannel);
        return blockingStub.withDeadlineAfter(5000, TimeUnit.MILLISECONDS);
    }

}
