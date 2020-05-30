package com.webank.ai.fate.serving.admin.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceGrpc;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceProto;
import com.webank.ai.fate.api.networking.common.CommonServiceGrpc;
import com.webank.ai.fate.api.networking.common.CommonServiceProto;
import com.webank.ai.fate.serving.core.bean.GrpcConnectionPool;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NegotiationType;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Description TODO
 * @Author kaideng
 **/

@RequestMapping("/api")
@RestController
public class MonitorController {

    GrpcConnectionPool grpcConnectionPool = GrpcConnectionPool.getPool();

    @GetMapping("/monitor/queryJvm")

    public  List  queryJvmData(){

        try {
            long  now  = System.currentTimeMillis();

            NettyChannelBuilder channelBuilder = NettyChannelBuilder
                    .forAddress("localhost", 8000)
                    .keepAliveTime(60, TimeUnit.SECONDS)
                    .keepAliveTimeout(60, TimeUnit.SECONDS)
                    .keepAliveWithoutCalls(true)
                    .idleTimeout(60, TimeUnit.SECONDS)
                    .perRpcBufferLimit(128 << 20)
                    .flowControlWindow(32 << 20)
                    .maxInboundMessageSize(32 << 20)
                    .enableRetry()
                    .retryBufferSize(16 << 20)
                    .maxRetryAttempts(20);      // todo: configurable
            channelBuilder.negotiationType(NegotiationType.PLAINTEXT)
                    .usePlaintext();

            ManagedChannel managedChannel =  channelBuilder.build();
            CommonServiceGrpc.CommonServiceBlockingStub blockingStub = CommonServiceGrpc.newBlockingStub(managedChannel);
            blockingStub = blockingStub.withDeadlineAfter(2000, TimeUnit.MILLISECONDS);

            CommonServiceProto.QueryJvmInfoRequest.Builder  builder = CommonServiceProto.QueryJvmInfoRequest.newBuilder();

            CommonServiceProto.CommonResponse commonResponse =blockingStub.queryJvmInfo(builder.build());
            if(commonResponse.getData()!=null) {
                String data = new String(commonResponse.getData().toByteArray());
                List  result =JSON.parseObject(data,List.class);
                return  result;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return  null;
    }



    @GetMapping("/monitor/query")

    public  List  queryMonitorData(){


        try {
            long  now  = System.currentTimeMillis();

            NettyChannelBuilder channelBuilder = NettyChannelBuilder
                    .forAddress("localhost", 8000)
                    .keepAliveTime(60, TimeUnit.SECONDS)
                    .keepAliveTimeout(60, TimeUnit.SECONDS)
                    .keepAliveWithoutCalls(true)
                    .idleTimeout(60, TimeUnit.SECONDS)
                    .perRpcBufferLimit(128 << 20)
                    .flowControlWindow(32 << 20)
                    .maxInboundMessageSize(32 << 20)
                    .enableRetry()
                    .retryBufferSize(16 << 20)
                    .maxRetryAttempts(20);      // todo: configurable
            channelBuilder.negotiationType(NegotiationType.PLAINTEXT)
                    .usePlaintext();

            ManagedChannel managedChannel =  channelBuilder.build();
                    CommonServiceGrpc.CommonServiceBlockingStub blockingStub = CommonServiceGrpc.newBlockingStub(managedChannel);
            blockingStub = blockingStub.withDeadlineAfter(2000, TimeUnit.MILLISECONDS);

            CommonServiceProto.QueryMetricRequest.Builder  builder = CommonServiceProto.QueryMetricRequest.newBuilder();

            builder.setBeginMs(now -5000);

            builder.setEndMs(now);

            CommonServiceProto.CommonResponse commonResponse =blockingStub.queryMetrics(builder.build());
            if(commonResponse.getData()!=null) {
                String data = new String(commonResponse.getData().toByteArray());
                List  result =JSON.parseObject(data,List.class);
                return  result;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return  null;
    }




    private CommonServiceGrpc.CommonServiceFutureStub getCommonServiceFutureStub(String host, int port) throws Exception {
        Preconditions.checkArgument(StringUtils.isNotBlank(host), "parameter host is blank");
        Preconditions.checkArgument(port != 0, "parameter port was wrong");
        ManagedChannel managedChannel = grpcConnectionPool.getManagedChannel(host, port);
        CommonServiceGrpc.CommonServiceFutureStub futureStub = CommonServiceGrpc.newFutureStub(managedChannel);
        return futureStub;
    }







}
