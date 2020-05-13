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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.ByteString;
import com.webank.ai.fate.api.networking.proxy.DataTransferServiceGrpc;
import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.register.router.RouterService;
import com.webank.ai.fate.register.url.CollectionUtils;
import com.webank.ai.fate.register.url.URL;
import com.webank.ai.fate.serving.MetaInfo;
import com.webank.ai.fate.serving.core.async.AsyncMessageEvent;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.cache.Cache;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.model.Model;
import com.webank.ai.fate.serving.core.rpc.core.FederatedRpcInvoker;
import com.webank.ai.fate.serving.core.utils.DisruptorUtil;
import com.webank.ai.fate.serving.core.utils.EncryptUtils;
import com.webank.ai.fate.serving.event.CacheEventData;
import io.grpc.ManagedChannel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class DefaultFederatedRpcInvoker implements FederatedRpcInvoker<Proxy.Packet> {

    private static final Logger logger = LoggerFactory.getLogger(FederatedRpcInvoker.class);
    @Autowired(required = false)
    public RouterService routerService;
    @Autowired
    private Environment environment;
    @Autowired
    private Cache cache;

    private Proxy.Packet build(Context context, RpcDataWraper rpcDataWraper) {

        Model model = ((ServingServerContext) context).getModel();
        Preconditions.checkArgument(model != null);
        Proxy.Packet.Builder packetBuilder = Proxy.Packet.newBuilder();
        packetBuilder.setBody(Proxy.Data.newBuilder()
                .setValue(ByteString.copyFrom(JSON.toJSONBytes(rpcDataWraper.getData())))
                .build());

        Proxy.Metadata.Builder metaDataBuilder = Proxy.Metadata.newBuilder();
        Proxy.Topic.Builder topicBuilder = Proxy.Topic.newBuilder();

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
        String version = Long.toString(MetaInfo.currentVersion);
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


    private String route() {

        boolean routerByzk = environment.getProperty(Dict.USE_ZK_ROUTER, boolean.class, Boolean.TRUE);
        String address = null;
        if (!routerByzk) {
            address = environment.getProperty(Dict.PROPERTY_PROXY_ADDRESS);
        } else {
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


    private String buildCacheKey(Model guestModel, Model hostModel, Map sendToRemote) {

        String tableName = guestModel.getTableName();
        String namespace = guestModel.getNamespace();
        String partId = hostModel.getPartId();
        StringBuilder sb = new StringBuilder();
        sb.append(partId).append(tableName).append(namespace).append(JSON.toJSONString(sendToRemote));
        String key = EncryptUtils.encrypt(sb.toString(), EncryptMethod.MD5);
        return key;


    }

    @Override
    public ListenableFuture<ReturnResult> singleInferenceRpcWithCache(Context context,
                                                                      RpcDataWraper rpcDataWraper, boolean useCache) {

        InferenceRequest inferenceRequest = (InferenceRequest) rpcDataWraper.getData();
        if (useCache) {
            Object result = cache.get(buildCacheKey(rpcDataWraper.getGuestModel(), rpcDataWraper.getHostModel(), inferenceRequest.getSendToRemoteFeatureData()));
            if (result != null) {
                Map data = JSON.parseObject(result.toString(), Map.class);
                ReturnResult returnResult = new ReturnResult();
                returnResult.setRetcode(StatusCode.SUCCESS);
                returnResult.setRetmsg("hit cache");
                return new AbstractFuture<ReturnResult>() {
                    @Override
                    public ReturnResult get() throws InterruptedException, ExecutionException {
                        returnResult.setData(data);
                        return returnResult;
                    }

                    @Override
                    public ReturnResult get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                        returnResult.setData(data);
                        return returnResult;
                    }

                };
            }

        }
        ListenableFuture<Proxy.Packet> future = this.async(context, rpcDataWraper);
        return new AbstractFuture<ReturnResult>() {


            @Override
            public ReturnResult get() throws InterruptedException, ExecutionException {
                return parse(future.get());
            }

            @Override
            public ReturnResult get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return parse(future.get(timeout, unit));
            }

            private ReturnResult parse(Proxy.Packet remote) {
                if (remote != null) {
                    String remoteContent = remote.getBody().getValue().toStringUtf8();
                    ReturnResult remoteInferenceResult = (ReturnResult) JSON.parseObject(remoteContent, ReturnResult.class);
                    if (useCache && StatusCode.SUCCESS.equals(remoteInferenceResult.getRetcode())) {
                        try {
                            AsyncMessageEvent asyncMessageEvent = new AsyncMessageEvent();
                            CacheEventData cacheEventData = new CacheEventData(buildCacheKey(rpcDataWraper.getGuestModel(), rpcDataWraper.getHostModel(), inferenceRequest.getSendToRemoteFeatureData()), remoteInferenceResult.getData());
                            asyncMessageEvent.setName(Dict.EVENT_SET_INFERENCE_CACHE);
                            asyncMessageEvent.setData(cacheEventData);
                            DisruptorUtil.producer(asyncMessageEvent);
                        } catch (Exception e) {
                            logger.error("send cache event error", e);
                        }
                    }
                    return remoteInferenceResult;
                } else {
                    return null;
                }
            }

        };

    }


    @Override
    public ListenableFuture<BatchInferenceResult> batchInferenceRpcWithCache(Context context,
                                                                             RpcDataWraper rpcDataWraper, boolean useCache) {

        BatchInferenceRequest inferenceRequest = (BatchInferenceRequest) rpcDataWraper.getData();
        Map<Integer, BatchInferenceResult.SingleInferenceResult> cacheData = Maps.newHashMap();
        if (useCache) {
            List<BatchInferenceRequest.SingleInferenceData> listData = inferenceRequest.getBatchDataList();
            List<String> cacheKeys = Lists.newArrayList();
            Map<String, List<Integer>> keyIndexMap = Maps.newHashMap();
            for (int i = 0; i < listData.size(); i++) {
                BatchInferenceRequest.SingleInferenceData singleInferenceData = listData.get(i);
                String key = buildCacheKey(rpcDataWraper.getGuestModel(), rpcDataWraper.getHostModel(), singleInferenceData.getSendToRemoteFeatureData());
                cacheKeys.add(key);
                if (keyIndexMap.get(key) == null) {
                    keyIndexMap.put(key, Lists.newArrayList(i));
                } else {
                    keyIndexMap.get(key).add(i);
                }

            }
            ;
            if (CollectionUtils.isNotEmpty(cacheKeys)) {
                List<Cache.DataWrapper<String, String>> dataWrapperList = this.cache.get(cacheKeys.toArray());

                if (dataWrapperList != null) {


                    Set<Integer> prepareToRemove = Sets.newHashSet();
                    for (Cache.DataWrapper<String, String> cacheDataWrapper : dataWrapperList) {
                        String key = cacheDataWrapper.getKey();
                        List<Integer> indexs = keyIndexMap.get(key);
                        if (indexs != null) {
//                        System.err.println("index "+ index);
//                        BatchInferenceRequest.SingleInferenceData removedRequest = listData.remove(index.intValue());
                            prepareToRemove.addAll(indexs);
                            for (Integer index : indexs) {
                                String value = cacheDataWrapper.getValue();
                                Map data = JSON.parseObject(value, Map.class);
                                BatchInferenceResult.SingleInferenceResult finalSingleResult = new BatchInferenceResult.SingleInferenceResult();
                                finalSingleResult.setRetcode(StatusCode.SUCCESS);
                                finalSingleResult.setData(data);
                                finalSingleResult.setRetmsg("hit cache");
                                finalSingleResult.setIndex(listData.get(index).getIndex());
                                cacheData.put(listData.get(index).getIndex(), finalSingleResult);
                            }
                        }
                    }

                    List<BatchInferenceRequest.SingleInferenceData> newRequestList = Lists.newArrayList();

                    for (int index = 0; index < listData.size(); index++) {
                        if (!prepareToRemove.contains(index)) {
                            newRequestList.add(listData.get(index));
                        }

                    }
                    inferenceRequest.getBatchDataList().clear();
                    inferenceRequest.getBatchDataList().addAll(newRequestList);

                }
            }
        }

        ListenableFuture<Proxy.Packet> future = null;

        if (inferenceRequest.getBatchDataList().size() > 0) {
//            logger.info("iiiiiiiiiiiiiiiiiiiiii {}", inferenceRequest.getBatchDataList().size());
            future = this.async(context, rpcDataWraper);
        }
        return new BatchInferenceFuture(future, rpcDataWraper, inferenceRequest, useCache, cacheData);
    }


    @Override
    public ListenableFuture<Proxy.Packet> async(Context context, RpcDataWraper rpcDataWraper) {

        Proxy.Packet packet = this.build(context, rpcDataWraper);

        Proxy.Packet resultPacket = null;

        String address = this.route();

        GrpcConnectionPool grpcConnectionPool = GrpcConnectionPool.getPool();

        Preconditions.checkArgument(StringUtils.isNotEmpty(address));

        ManagedChannel channel1 = grpcConnectionPool.getManagedChannel(address);

        ListenableFuture<Proxy.Packet> future = null;

        DataTransferServiceGrpc.DataTransferServiceFutureStub stub1 = DataTransferServiceGrpc.newFutureStub(channel1);
        context.setDownstreamBegin(System.currentTimeMillis());
        future = stub1.unaryCall(packet);
        return future;


    }


}
