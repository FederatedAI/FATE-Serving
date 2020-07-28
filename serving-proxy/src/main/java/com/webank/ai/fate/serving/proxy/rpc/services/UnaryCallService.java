package com.webank.ai.fate.serving.proxy.rpc.services;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.ByteString;
import com.webank.ai.fate.api.networking.proxy.DataTransferServiceGrpc;
import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.GrpcConnectionPool;
import com.webank.ai.fate.serving.core.bean.NettyServerInfo;
import com.webank.ai.fate.serving.core.exceptions.ErrorCode;
import com.webank.ai.fate.serving.core.rpc.core.AbstractServiceAdaptor;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.ProxyService;
import com.webank.ai.fate.serving.core.rpc.grpc.GrpcType;
import com.webank.ai.fate.serving.core.rpc.router.RouterInfo;
import com.webank.ai.fate.serving.metrics.api.IMetricFactory;


import com.webank.ai.fate.serving.proxy.security.AuthUtils;
import io.grpc.ManagedChannel;
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
@ProxyService(name = "unaryCall",preChain = {
        "overloadMonitor",
       "federationParamValidator",
       "defaultAuthentication",
        "defaultServingRouter"})

public class UnaryCallService extends AbstractServiceAdaptor<Proxy.Packet, Proxy.Packet> {
    @Autowired
    IMetricFactory metricFactory;


    GrpcConnectionPool grpcConnectionPool= GrpcConnectionPool.getPool();

    @Autowired
    AuthUtils authUtils;

    @Value("${proxy.grpc.unaryCall.timeout:3000}")
    private  int  timeout;

    @Value("${proxy.grpc.inter.negotiationType:PLAINTEXT}")
    private String negotiationType;

    @Value("${proxy.grpc.inter.server.certChain.file:}")
    private String certChainFilePath;

    @Value("${proxy.grpc.inter.server.privateKey.file:}")
    private String privateKeyFilePath;

    @Value("${proxy.grpc.inter.CA.file:}")
    private String trustCertCollectionFilePath;

    Logger logger  = LoggerFactory.getLogger(UnaryCallService.class);

    static  final  String  RETURN_CODE= "retcode";

    @Override
    public Proxy.Packet doService(Context context, InboundPackage<Proxy.Packet> data, OutboundPackage<Proxy.Packet> outboundPackage) {

        RouterInfo routerInfo = data.getRouterInfo();
        ManagedChannel managedChannel = null;
        try {
            Proxy.Packet  sourcePackage = data.getBody();
            sourcePackage = authUtils.addAuthInfo(sourcePackage);
            NettyServerInfo nettyServerInfo = null;
            if(GrpcType.INTER_GRPC == context.getGrpcType()) {
                nettyServerInfo = new NettyServerInfo(negotiationType, certChainFilePath, privateKeyFilePath, trustCertCollectionFilePath);
            } else {
                nettyServerInfo = new NettyServerInfo();
            }
            managedChannel =   grpcConnectionPool.getManagedChannel(routerInfo.getHost(), routerInfo.getPort(), nettyServerInfo);
            DataTransferServiceGrpc.DataTransferServiceFutureStub stub1 = DataTransferServiceGrpc.newFutureStub(managedChannel);

            stub1.withDeadlineAfter(timeout, TimeUnit.MILLISECONDS);

            metricFactory.counter("grpc.unaryCall.service", "in doService", "direction", "out", "result", "success").increment();

            context.setDownstreamBegin(System.currentTimeMillis());

            ListenableFuture<Proxy.Packet> future= stub1.unaryCall(sourcePackage);

            Proxy.Packet packet = future.get(timeout,TimeUnit.MILLISECONDS);

            metricFactory.counter("grpc.unaryCall.service", "in doService", "direction", "in", "result", "success").increment();

            return  packet;

        } catch (Exception e) {
            metricFactory.counter("grpc.unaryCall.service", "in doService", "direction", "in", "result", "error").increment();

            e.printStackTrace();
            logger.error("unaryCall error ",e);
        }finally {
            long  end =  System.currentTimeMillis();
            context.setDownstreamCost(end - context.getDownstreamBegin());
        }
        return  null;
    }

    @Override
    protected Proxy.Packet transformErrorMap(Context  context,Map data) {
        Proxy.Packet.Builder  builder = Proxy.Packet.newBuilder();
        Proxy.Data.Builder dataBuilder =  Proxy.Data.newBuilder();
        Map  fateMap = Maps.newHashMap();
        fateMap.put("retcode",transformErrorCode(data.get(Dict.CODE).toString()));
        fateMap.put("retmsg",data.get(Dict.MESSAGE));
        builder.setBody(dataBuilder.setValue(ByteString.copyFromUtf8(JSON.toJSONString(fateMap))));
        return builder.build();
    }


    static  Map<String,String>  fateErrorCodeMap = Maps.newHashMap();

    static {
        fateErrorCodeMap.put(ErrorCode.PARAM_ERROR,"500");
        fateErrorCodeMap.put(ErrorCode.ROLE_ERROR,"501");
        fateErrorCodeMap.put(ErrorCode.SERVICE_NOT_FOUND,"502");
        fateErrorCodeMap.put(ErrorCode.SYSTEM_ERROR,"503");
        fateErrorCodeMap.put(ErrorCode.LIMIT_ERROR,"504");
        fateErrorCodeMap.put(ErrorCode.NET_ERROR,"507");
        fateErrorCodeMap.put(ErrorCode.SHUTDOWN_ERROR,"508");
        fateErrorCodeMap.put(ErrorCode.ROUTER_ERROR,"509");
    }

    private String  transformErrorCode(String errorCode){
            String  result = fateErrorCodeMap.get(errorCode);
            if( result!=null){

                return  fateErrorCodeMap.get(errorCode);

            }else {
                return "";
            }

    }

}
