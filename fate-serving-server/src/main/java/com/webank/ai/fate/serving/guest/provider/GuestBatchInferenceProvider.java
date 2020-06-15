package com.webank.ai.fate.serving.guest.provider;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.exceptions.BaseException;
import com.webank.ai.fate.serving.core.model.Model;
import com.webank.ai.fate.serving.core.model.ModelProcessor;
import com.webank.ai.fate.serving.core.rpc.core.FateService;
import com.webank.ai.fate.serving.core.rpc.core.FederatedRpcInvoker;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;


@FateService(name = "batchInference", preChain = {
        "requestOverloadBreaker",
        "guestBatchParamInterceptor",
        "guestModelInterceptor",
        "federationRouterInterceptor"
}, postChain = {

})
@Service
public class GuestBatchInferenceProvider extends AbstractServingServiceProvider<BatchInferenceRequest, BatchInferenceResult> {

    @Autowired
    FederatedRpcInvoker federatedRpcInvoker;

    @Override
    public BatchInferenceResult doService(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) {
        Model model = ((ServingServerContext) context).getModel();
        ModelProcessor modelProcessor = model.getModelProcessor();
        BatchInferenceRequest batchInferenceRequest = (BatchInferenceRequest) inboundPackage.getBody();
        Map futureMap = Maps.newHashMap();
        model.getFederationModelMap().forEach((hostPartyId, remoteModel) -> {
            BatchHostFederatedParams batchHostFederatedParams = buildBatchHostFederatedParams(context, batchInferenceRequest, model, remoteModel);
            ListenableFuture<BatchInferenceResult> originBatchResultFuture = federatedRpcInvoker.batchInferenceRpcWithCache(context, buildRpcDataWraper(model, remoteModel, batchHostFederatedParams), MetaInfo.PROPERTY_REMOTE_MODEL_INFERENCE_RESULT_CACHE_SWITCH);
            futureMap.put(hostPartyId, originBatchResultFuture);
        });
        BatchInferenceResult batchFederatedResult = modelProcessor.guestBatchInference(context, batchInferenceRequest, futureMap, MetaInfo.BATCH_INFERENCE_RPC_TIMEOUT);
        batchFederatedResult.setCaseid(context.getCaseId());
        return batchFederatedResult;
    }

    @Override
    protected OutboundPackage<BatchInferenceResult> serviceFailInner(Context context, InboundPackage<BatchInferenceRequest> data, Throwable e) {
        OutboundPackage<BatchInferenceResult> outboundPackage = new OutboundPackage<BatchInferenceResult>();
        BatchInferenceResult batchInferenceResult = new BatchInferenceResult();
        if (e instanceof BaseException) {
            BaseException baseException = (BaseException) e;
            batchInferenceResult.setRetcode(baseException.getRetcode());
            batchInferenceResult.setRetmsg(e.getMessage());
        } else {
            batchInferenceResult.setRetcode(StatusCode.SYSTEM_ERROR);
        }
        context.setReturnCode(batchInferenceResult.getRetcode());
        outboundPackage.setData(batchInferenceResult);
        return outboundPackage;
    }

    private FederatedRpcInvoker.RpcDataWraper buildRpcDataWraper(Model model, Model remoteModel, Object batchHostFederatedParams) {
        FederatedRpcInvoker.RpcDataWraper rpcDataWraper = new FederatedRpcInvoker.RpcDataWraper();
        rpcDataWraper.setGuestModel(model);
        rpcDataWraper.setHostModel(remoteModel);
        rpcDataWraper.setData(batchHostFederatedParams);
        rpcDataWraper.setRemoteMethodName(Dict.REMOTE_METHOD_BATCH);
        return rpcDataWraper;
    }


}
