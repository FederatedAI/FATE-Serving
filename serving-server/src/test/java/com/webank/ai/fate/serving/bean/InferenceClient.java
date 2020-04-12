package com.webank.ai.fate.serving.bean;

import com.google.protobuf.ByteString;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceGrpc;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceProto;
import com.webank.ai.fate.api.serving.InferenceServiceGrpc;
import com.webank.ai.fate.api.serving.InferenceServiceProto;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NegotiationType;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;

import java.util.concurrent.TimeUnit;

public class InferenceClient {

    public   InferenceClient(String ip,Integer port){
            this.ip = ip;
            this.port = port;
    }

    protected  String    ip;
    protected  Integer   port;


    public static ManagedChannel createManagedChannel(String ip, int port) throws Exception {


        NettyChannelBuilder builder = NettyChannelBuilder
                .forAddress(ip, port)
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
        builder.negotiationType(NegotiationType.PLAINTEXT)
                .usePlaintext();

        return builder.build();


    }


    public   InferenceServiceProto.InferenceMessage  batchInference(byte[]  data){
        ManagedChannel  managedChannel=null;
        try {
            managedChannel =  createManagedChannel(ip,port);
        } catch (Exception e) {
            e.printStackTrace();
        }

        InferenceServiceGrpc.InferenceServiceBlockingStub blockingStub = InferenceServiceGrpc.newBlockingStub(managedChannel);
        InferenceServiceProto.InferenceMessage.Builder inferenceMessageBuilder =
                InferenceServiceProto.InferenceMessage.newBuilder();

        inferenceMessageBuilder.setBody(ByteString.copyFrom(data));

        return  blockingStub.batchInference(inferenceMessageBuilder.build());

    }

    public   InferenceServiceProto.InferenceMessage  inference(byte[]  data){
        ManagedChannel  managedChannel=null;
        try {
          managedChannel =  createManagedChannel(ip,port);
        } catch (Exception e) {
            e.printStackTrace();
        }

        InferenceServiceGrpc.InferenceServiceBlockingStub blockingStub = InferenceServiceGrpc.newBlockingStub(managedChannel);
        InferenceServiceProto.InferenceMessage.Builder inferenceMessageBuilder =
                InferenceServiceProto.InferenceMessage.newBuilder();

        inferenceMessageBuilder.setBody(ByteString.copyFrom(data));

        return  blockingStub.inference(inferenceMessageBuilder.build());

    }



    public  ModelServiceProto.PublishResponse  load(ModelServiceProto.PublishRequest publishRequest){
        ManagedChannel  managedChannel=null;
        try {
            managedChannel =  createManagedChannel(ip,port);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //PublishRequest

        ModelServiceGrpc.ModelServiceBlockingStub blockingStub = ModelServiceGrpc.newBlockingStub(managedChannel);

        return  blockingStub.publishLoad(publishRequest);

    }

    public  ModelServiceProto.PublishResponse  bind(ModelServiceProto.PublishRequest publishRequest){
        ManagedChannel  managedChannel=null;
        try {
            managedChannel =  createManagedChannel(ip,port);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //PublishRequest

        ModelServiceGrpc.ModelServiceBlockingStub blockingStub = ModelServiceGrpc.newBlockingStub(managedChannel);

        return  blockingStub.publishBind(publishRequest);

    }







}
