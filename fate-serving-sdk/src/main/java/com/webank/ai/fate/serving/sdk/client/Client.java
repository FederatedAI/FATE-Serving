package com.webank.ai.fate.serving.sdk.client;


import com.google.common.base.Preconditions;
import com.google.protobuf.ByteString;
import com.webank.ai.fate.api.serving.InferenceServiceGrpc;
import com.webank.ai.fate.api.serving.InferenceServiceProto;
import com.webank.ai.fate.register.router.RouterService;
import com.webank.ai.fate.register.url.URL;
import com.webank.ai.fate.serving.core.bean.GrpcConnectionPool;
import com.webank.ai.fate.serving.core.bean.InferenceRequest;
import com.webank.ai.fate.serving.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import io.grpc.ManagedChannel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.List;

public class Client {

    RouterService routerService;

    GrpcConnectionPool  grpcConnectionPool =  GrpcConnectionPool.getPool();

    final String PROJECT ="serving";
    final String SINGLE_INFERENCE = "inference";


    public ReturnResult singleInference(InferenceRequest  request) {
        Preconditions.checkArgument(request!=null);
        Preconditions.checkArgument(StringUtils.isNotEmpty(request.getServiceId()));
        List<URL> urls = routerService.router(PROJECT,request.getServiceId(),SINGLE_INFERENCE);
        Preconditions.checkArgument(CollectionUtils.isNotEmpty());


        ManagedChannel managedChannel = null;
        try {

             managedChannel = grpcConnectionPool.getManagedChannel(ip, port);
        } catch (Exception e) {
            e.printStackTrace();
        }


//        URL targetUrl = URL.valueOf("serving/" + serviceId + "/inference");
//
//        FdnServerRouterInfo result = new FdnServerRouterInfo();
//
//        List<URL> list = this.zkRouterService.router(targetUrl);
        InferenceServiceProto.InferenceMessage  =n

//        InferenceRequest inferenceRequest = new InferenceRequest();
//
//        inferenceRequest.setServiceId("fm");
//
//        inferenceRequest.getFeatureData().put("x0", 0.100016);
//        inferenceRequest.getFeatureData().put("x1", 1.210);
//        inferenceRequest.getFeatureData().put("x2", 2.321);
//        inferenceRequest.getFeatureData().put("x3", 3.432);
//        inferenceRequest.getFeatureData().put("x4", 4.543);
//        inferenceRequest.getFeatureData().put("x5", 5.654);
//        inferenceRequest.getFeatureData().put("x6", 5.654);
//        inferenceRequest.getFeatureData().put("x7", 0.102345);

        inferenceRequest.getSendToRemoteFeatureData().putAll(inferenceRequest.getFeatureData());

        InferenceServiceProto.InferenceMessage.Builder inferenceMessageBuilder =
                InferenceServiceProto.InferenceMessage.newBuilder();
        String contentString = JsonUtil.object2Json(inferenceRequest);
        System.err.println("send data ===" + contentString);
        try {
            inferenceMessageBuilder.setBody(ByteString.copyFrom(contentString, "UTF-8"));

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        InferenceServiceProto.InferenceMessage inferenceMessage = inferenceMessageBuilder.build();

        System.err.println(inferenceMessage.getBody());

        InferenceServiceProto.InferenceMessage resultMessage = inferenceClient.inference(inferenceMessage);



        InferenceServiceGrpc.InferenceServiceBlockingStub blockingStub = InferenceServiceGrpc.newBlockingStub(managedChannel);
        return blockingStub.batchInference(data);
    }

}
