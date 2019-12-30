package com.webank.ai.fate.serving.proxy.rpc.services;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.ByteString;
import com.webank.ai.fate.api.serving.InferenceServiceGrpc;
import com.webank.ai.fate.api.serving.InferenceServiceProto;
import com.webank.ai.fate.serving.metrics.api.IMetricFactory;
import com.webank.ai.fate.serving.proxy.common.Dict;
import com.webank.ai.fate.serving.proxy.exceptions.NoResultException;
import com.webank.ai.fate.serving.proxy.rpc.core.*;
import com.webank.ai.fate.serving.proxy.rpc.grpc.GrpcConnectionPool;
import com.webank.ai.fate.serving.proxy.rpc.router.RouterInfo;
import io.grpc.ManagedChannel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
@ProxyService(name = "inference",preChain = {
        "overloadMonitor",
        "inferenceParamValidator",
        "defaultServingRouter"})

public class InferenceService extends AbstractServiceAdaptor<Map,Map >   {

    Logger logger  = LoggerFactory.getLogger(InferenceService.class);

    @Autowired
    IMetricFactory metricFactory;

    @Autowired
    GrpcConnectionPool   grpcConnectionPool;

    public InferenceService() {}

    @Value("${proxy.grpc.inference.timeout:3000}")
    private  int  timeout;

    @Override
    public Map doService(Context context, InboundPackage<Map> data, OutboundPackage<Map> outboundPackage) {

        Map  resultMap = Maps.newHashMap();
        RouterInfo routerInfo = data.getRouterInfo();

        ManagedChannel  managedChannel = null;

        String   resultString=null;
        try {
            try {
                logger.info("try to get grpc connection");
                managedChannel = this.grpcConnectionPool.getManagedChannel(routerInfo.getHost(), routerInfo.getPort());

            } catch (Exception e) {
                logger.error("get grpc channel error", e);
                throw new NoResultException();
            }
            try {
                Map reqBodyMap  = data.getBody();
                Map reqHeadMap = data.getHead();

                Map  inferenceReqMap = Maps.newHashMap();
                inferenceReqMap.put(Dict.CASE_ID, context.getCaseId());
                inferenceReqMap.putAll(reqHeadMap);
                inferenceReqMap.put(Dict.FEATURE_DATA,Maps.newHashMap(reqBodyMap));

                logger.info("inference req ============================= {}", JSON.toJSONString(inferenceReqMap));

                InferenceServiceProto.InferenceMessage.Builder reqBuilder = InferenceServiceProto.InferenceMessage.newBuilder();
                reqBuilder.setBody(ByteString.copyFrom(JSON.toJSONString(inferenceReqMap).getBytes()));

                InferenceServiceGrpc.InferenceServiceFutureStub futureStub = InferenceServiceGrpc.newFutureStub(managedChannel);

                metricFactory.counter("http.inference", "send to self serving server", "send", "self.serving-server").increment();

                ListenableFuture<InferenceServiceProto.InferenceMessage> resultFuture = futureStub.inference(reqBuilder.build());
                InferenceServiceProto.InferenceMessage result = resultFuture.get(timeout,TimeUnit.MILLISECONDS);
                metricFactory.counter("http.inference", "receive from self serving server", "receive", "self.serving-server", "result", "success").increment();
                logger.info("routerinfo {} send {} result {}",routerInfo,inferenceReqMap,result);
                resultString = new String(result.getBody().toByteArray());
            } catch (Exception e) {
                metricFactory.counter("http.inference", "receive from self serving server", "receive", "self.serving-server", "result", "grpc.error").increment();
                logger.error("get grpc result error", e);
                throw new NoResultException();
            }
        }
        finally {
            if(managedChannel!=null) {
                grpcConnectionPool.returnPool(managedChannel, routerInfo.getHost(), routerInfo.getPort());
            }
        }
        if(StringUtils.isNotEmpty(resultString)){
          resultMap =  JSON.parseObject(resultString,Map.class);
        }
        return  resultMap;
    }

    @Override
    protected Map transformErrorMap(Context context,Map data) {
        return  data;
    }
}



