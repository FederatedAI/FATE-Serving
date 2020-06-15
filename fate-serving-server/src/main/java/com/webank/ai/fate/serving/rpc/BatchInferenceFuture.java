package com.webank.ai.fate.serving.rpc;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ListenableFuture;
import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.serving.core.async.AsyncMessageEvent;
import com.webank.ai.fate.serving.core.bean.BatchInferenceRequest;
import com.webank.ai.fate.serving.core.bean.BatchInferenceResult;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.EncryptMethod;
import com.webank.ai.fate.serving.core.cache.Cache;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.model.Model;
import com.webank.ai.fate.serving.core.rpc.core.FederatedRpcInvoker;
import com.webank.ai.fate.serving.core.utils.DisruptorUtil;
import com.webank.ai.fate.serving.core.utils.EncryptUtils;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class BatchInferenceFuture extends AbstractFuture {


    Logger logger = LoggerFactory.getLogger(BatchInferenceFuture.class);
    ListenableFuture<Proxy.Packet> future;
    FederatedRpcInvoker.RpcDataWraper rpcDataWraper;
    BatchInferenceRequest batchInferenceRequest;
    boolean useCache;
    Map<Integer, BatchInferenceResult.SingleInferenceResult> cacheData;

    public BatchInferenceFuture(ListenableFuture<Proxy.Packet> future,
                                FederatedRpcInvoker.RpcDataWraper rpcDataWraper,
                                BatchInferenceRequest batchInferenceRequest,
                                boolean useCache,
                                Map<Integer, BatchInferenceResult.SingleInferenceResult> cacheData) {
        this.future = future;
        this.rpcDataWraper = rpcDataWraper;
        this.batchInferenceRequest = batchInferenceRequest;
        this.useCache = useCache;
        this.cacheData = cacheData;
    }

    @Override
    public BatchInferenceResult get() throws InterruptedException, ExecutionException {
        if (future != null) {
            BatchInferenceResult remoteBatchInferenceResult = parse(future.get());
            return mergeCache(remoteBatchInferenceResult);
        } else {
            return mergeCache(null);
        }

    }

    public BatchInferenceResult mergeCache(BatchInferenceResult remoteBatchInferenceResult) {
        if (cacheData != null && cacheData.size() > 0) {
            if (remoteBatchInferenceResult != null) {
                cacheData.forEach((k, v) -> {
                    remoteBatchInferenceResult.getBatchDataList().add(v);
                });
                remoteBatchInferenceResult.rebuild();
                return remoteBatchInferenceResult;
            } else {
                BatchInferenceResult newBatchInferenceResult = new BatchInferenceResult();
                newBatchInferenceResult.setRetcode(StatusCode.SUCCESS);
                cacheData.forEach((k, v) -> {
                    newBatchInferenceResult.getBatchDataList().add(v);
                });
                newBatchInferenceResult.rebuild();
                return newBatchInferenceResult;
            }
        } else {
            return remoteBatchInferenceResult;
        }
    }


    @Override
    public BatchInferenceResult get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (future != null) {
            BatchInferenceResult remoteBatchInferenceResult = parse(future.get(timeout, unit));
            return mergeCache(remoteBatchInferenceResult);
        } else {
            return mergeCache(null);
        }
    }


    private BatchInferenceResult parse(Proxy.Packet remote) {
        if (remote != null) {
            String remoteContent = remote.getBody().getValue().toStringUtf8();
            BatchInferenceResult remoteInferenceResult = JsonUtil.json2Object(remoteContent, BatchInferenceResult.class);
            if (useCache && StatusCode.SUCCESS.equals(remoteInferenceResult.getRetcode())) {
                try {
                    AsyncMessageEvent asyncMessageEvent = new AsyncMessageEvent();
                    List<Cache.DataWrapper> cacheEventDataList = Lists.newArrayList();
                    for (BatchInferenceRequest.SingleInferenceData singleInferenceData : batchInferenceRequest.getBatchDataList()) {
                        BatchInferenceResult.SingleInferenceResult singleInferenceResult = remoteInferenceResult.getSingleInferenceResultMap().get(singleInferenceData.getIndex());
                        if (singleInferenceResult != null && StatusCode.SUCCESS.equals(singleInferenceResult.getRetcode())) {
                            Cache.DataWrapper dataWrapper = new Cache.DataWrapper(buildCacheKey(rpcDataWraper.getGuestModel(), rpcDataWraper.getHostModel(),
                                    singleInferenceData.getSendToRemoteFeatureData()), JsonUtil.object2Json(singleInferenceResult.getData()));
                            cacheEventDataList.add(dataWrapper);
                        }
                    }
                    asyncMessageEvent.setName(Dict.EVENT_SET_BATCH_INFERENCE_CACHE);
                    asyncMessageEvent.setData(cacheEventDataList);
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


    private String buildCacheKey(Model guestModel, Model hostModel, Map sendToRemote) {
        String tableName = guestModel.getTableName();
        String namespace = guestModel.getNamespace();
        String partId = hostModel.getPartId();
        StringBuilder sb = new StringBuilder();
        sb.append(partId).append(tableName).append(namespace).append(JsonUtil.object2Json(sendToRemote));
        String key = EncryptUtils.encrypt(sb.toString(), EncryptMethod.MD5);
        return key;
    }


}
