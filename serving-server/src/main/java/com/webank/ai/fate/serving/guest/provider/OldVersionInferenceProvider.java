package com.webank.ai.fate.serving.guest.provider;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.serving.core.exceptions.BaseException;
import com.webank.ai.fate.serving.core.exceptions.ErrorCode;
import com.webank.ai.fate.serving.core.rpc.core.*;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.constant.InferenceRetCode;
import com.webank.ai.fate.serving.core.model.Model;
import com.webank.ai.fate.serving.core.model.ModelProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 *  主要兼容host为1.2.x版本的接口
 *
 **/
@FateService(name ="singleInference",  preChain= {
        "monitorInterceptor",
        "guestSingleParamInterceptor",
        "guestModelInterceptor",
        "federationRouterInterceptor"
      },postChain = {
        "defaultPostProcess",
        "monitorInterceptor"
})
@Service
@Deprecated
public class OldVersionInferenceProvider extends AbstractServingServiceProvider<InferenceRequest,ReturnResult>{

    @Autowired
    FederatedRpcInvoker federatedRpcInvoker;

    @Value("${inference.single.timeout:3000}")
    long timeout;

    @Override
    public ReturnResult doService(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) {
        Model  model = ((ServingServerContext)context).getModel();
        ModelProcessor modelProcessor = model.getModelProcessor();
        Map<String, Model> hostModelMap = model.getFederationModelMap();
        InferenceRequest inferenceRequest = (InferenceRequest) inboundPackage.getBody();
        Map<String,Future>  futureMap = Maps.newHashMap();
        hostModelMap.forEach((hostPartId,hostModel)->{
            FederatedRpcInvoker.RpcDataWraper rpcDataWraper = new  FederatedRpcInvoker.RpcDataWraper();
            rpcDataWraper.setGuestModel(model);
            rpcDataWraper.setHostModel(hostModel);
            rpcDataWraper.setRemoteMethodName(Dict.FEDERATED_INFERENCE);
            rpcDataWraper.setData(inferenceRequest);
            ListenableFuture<Proxy.Packet> future = federatedRpcInvoker.async(context, rpcDataWraper);
            futureMap.put(hostPartId,future);

        });
        ReturnResult returnResult = modelProcessor.guestInference(context, inferenceRequest, futureMap,timeout);
        return returnResult;
    }


    @Override
    protected  OutboundPackage<ReturnResult>  serviceFailInner(Context context, InboundPackage<InferenceRequest> data, Throwable e) {
        OutboundPackage<ReturnResult> outboundPackage = new OutboundPackage<ReturnResult>();
        ReturnResult returnResult = ErrorMessageUtil.handleExceptionToReturnResult(e);
        outboundPackage.setData(returnResult);
        return  outboundPackage;
    }

}
