package com.webank.ai.fate.serving.proxy.rpc.services;


import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.ByteString;
import com.webank.ai.fate.api.serving.InferenceServiceGrpc;
import com.webank.ai.fate.api.serving.InferenceServiceProto;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.GrpcConnectionPool;
import com.webank.ai.fate.serving.core.exceptions.NoResultException;
import com.webank.ai.fate.serving.core.exceptions.UnSupportMethodException;
import com.webank.ai.fate.serving.core.rpc.core.AbstractServiceAdaptor;
import com.webank.ai.fate.serving.core.rpc.core.FateService;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.rpc.router.RouterInfo;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import io.grpc.ManagedChannel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@FateService(name = Dict.SERVICENAME_BATCH_INFERENCE, preChain = {
        "requestOverloadBreaker",
        "inferenceParamValidator",
        "defaultServingRouter"})
public class BatchInferenceService extends AbstractServiceAdaptor<Map, Map> {

    Logger logger = LoggerFactory.getLogger(BatchInferenceService.class);

    GrpcConnectionPool grpcConnectionPool = GrpcConnectionPool.getPool();

    @Value("${proxy.grpc.batch.inference.timeout:10000}")
    private int timeout;
//    @Value("${proxy.grpc.batch.inference.async.timeout:10000}")
//    private int asyncTimeout;

    public BatchInferenceService() {
    }

    /*{
            "head": {
                "serviceId": "2020040111152695637611"
            },
            "body": {
                "batchDataList": [
                    {
                        "index": 0,
                        "featureData": {
                            "x0": 1.88669,
                            "x1": -1.359293,
                            "x2": 2.303601,
                            "x3": 2.00137,
                            "x4": 1.307686
                        },
                        "sendToRemoteFeatureData": {
                            "device_id": "aaaaa",
                            "phone_num": "122222222"
                        }
                    },
                    {
                        "index": 1,
                        "featureData": {
                            "x0": 1.88669,
                            "x1": -1.359293,
                            "x2": 2.303601,
                            "x3": 2.00137,
                            "x4": 1.307686
                        },
                        "sendToRemoteFeatureData": {
                            "device_id": "aaaaa",
                            "phone_num": "122222222"
                        }
                    }
                ]
            }
    }*/

    @Override
    public Map doService(Context context, InboundPackage<Map> data, OutboundPackage<Map> outboundPackage) {

        Map resultMap = Maps.newHashMap();
        RouterInfo routerInfo = data.getRouterInfo();

        ManagedChannel managedChannel = null;

        String resultString = null;
        String callName = context.getCallName();
        ListenableFuture<InferenceServiceProto.InferenceMessage> resultFuture;

        try {
            if (logger.isDebugEnabled()) {
                logger.debug("try to get grpc connection");
            }
            managedChannel = this.grpcConnectionPool.getManagedChannel(routerInfo.getHost(), routerInfo.getPort());

        } catch (Exception e) {
            logger.error("get grpc channel error", e);
            throw new NoResultException();
        }

        Map reqBodyMap = data.getBody();
        Map reqHeadMap = data.getHead();

        Map inferenceReqMap = Maps.newHashMap();
        inferenceReqMap.put(Dict.CASE_ID, context.getCaseId());
        inferenceReqMap.putAll(reqHeadMap);
        inferenceReqMap.putAll(reqBodyMap);

        if (logger.isDebugEnabled()) {
            logger.debug("batch inference req : {}", JsonUtil.object2Json(inferenceReqMap));
        }
        InferenceServiceProto.InferenceMessage.Builder reqBuilder = InferenceServiceProto.InferenceMessage.newBuilder();
        reqBuilder.setBody(ByteString.copyFrom(JsonUtil.object2Json(inferenceReqMap).getBytes()));

        InferenceServiceGrpc.InferenceServiceFutureStub futureStub = InferenceServiceGrpc.newFutureStub(managedChannel);

        if (callName.equals(Dict.SERVICENAME_BATCH_INFERENCE)) {
            resultFuture = futureStub.batchInference(reqBuilder.build());
        } else {
            logger.error("unknown callName {}.", callName);
            throw new UnSupportMethodException();
        }

        try {
            InferenceServiceProto.InferenceMessage result = resultFuture.get(timeout, TimeUnit.MILLISECONDS);
            logger.info("routerinfo {} send {} result {}", routerInfo, inferenceReqMap, result);
            resultString = new String(result.getBody().toByteArray());
        } catch (Exception e) {
            logger.error("get grpc result error", e);
            throw new NoResultException();
        }

        if (StringUtils.isNotEmpty(resultString)) {
            resultMap = JsonUtil.json2Object(resultString, Map.class);
        }
        return resultMap;
    }

    @Override
    protected Map transformExceptionInfo(Context context, ExceptionInfo exceptionInfo) {
        Map map = Maps.newHashMap();
        map.put(Dict.RET_CODE, exceptionInfo.getCode());
        map.put(Dict.RET_MSG, exceptionInfo.getMessage());
        return map;
    }
}



