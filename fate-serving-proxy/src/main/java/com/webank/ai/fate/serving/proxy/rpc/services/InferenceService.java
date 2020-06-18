package com.webank.ai.fate.serving.proxy.rpc.services;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.ByteString;
import com.webank.ai.fate.api.serving.InferenceServiceGrpc;
import com.webank.ai.fate.api.serving.InferenceServiceProto;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.GrpcConnectionPool;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
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

/**
 * @Description TODO
 * @Author
 **/
@Service

// TODO utu: may load from cfg file is a better choice compare to using annotation?
@FateService(name = Dict.SERVICENAME_INFERENCE, preChain = {
        "requestOverloadBreaker",
        "inferenceParamValidator",
        "defaultServingRouter"})

public class InferenceService extends AbstractServiceAdaptor<Map, Map> {

    Logger logger = LoggerFactory.getLogger(InferenceService.class);

    GrpcConnectionPool grpcConnectionPool = GrpcConnectionPool.getPool();

    private int timeout = MetaInfo.PROPERTY_PROXY_GRPC_INFERENCE_TIMEOUT;
    public InferenceService() {
    }

    @Override
    public Map doService(Context context, InboundPackage<Map> data, OutboundPackage<Map> outboundPackage) {

        Map resultMap = Maps.newHashMap();
        RouterInfo routerInfo = data.getRouterInfo();
        ManagedChannel managedChannel = null;
        String resultString = null;
        String callName = context.getCallName();
        ListenableFuture<InferenceServiceProto.InferenceMessage> resultFuture;

        try {
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
        int timeWait = timeout;

        if (logger.isDebugEnabled()) {
            logger.debug("inference req : {}", JsonUtil.object2Json(inferenceReqMap));
        }
        InferenceServiceProto.InferenceMessage.Builder reqBuilder = InferenceServiceProto.InferenceMessage.newBuilder();
        reqBuilder.setBody(ByteString.copyFrom(JsonUtil.object2Json(inferenceReqMap).getBytes()));
        InferenceServiceGrpc.InferenceServiceFutureStub futureStub = InferenceServiceGrpc.newFutureStub(managedChannel);
        if (callName.equals(Dict.SERVICENAME_INFERENCE)) {
            resultFuture = futureStub.inference(reqBuilder.build());
            timeWait = timeout;
        }
        else {
            logger.error("unknown callName {}.", callName);
            throw new UnSupportMethodException();
        }
        try {
            InferenceServiceProto.InferenceMessage result = resultFuture.get(timeWait, TimeUnit.MILLISECONDS);
            //logger.info("routerinfo {} send {} result {}", routerInfo, inferenceReqMap, result);
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



