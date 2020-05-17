package com.webank.ai.fate.serving.grpc.service;

import com.webank.ai.fate.api.networking.common.CommonServiceGrpc;
import com.webank.ai.fate.serving.common.provider.CommonServiceProvider;
import com.webank.ai.fate.serving.guest.provider.GuestSingleInferenceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

@Service
public class CommonService  extends CommonServiceGrpc.CommonServiceImplBase{

    @Autowired
    CommonServiceProvider commonServiceProvider;

    @Override
    public void queryMetrics(com.webank.ai.fate.api.networking.common.CommonServiceOuterClass.QueryMetricRequest request,
                             io.grpc.stub.StreamObserver<com.webank.ai.fate.api.networking.common.CommonServiceOuterClass.QueryMetricResponse> responseObserver) {


      //  commonServiceProvider.service();


    }

}
