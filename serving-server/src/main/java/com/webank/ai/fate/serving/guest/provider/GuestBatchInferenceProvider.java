package com.webank.ai.fate.serving.guest.provider;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.exceptions.BaseException;
import com.webank.ai.fate.serving.core.exceptions.ErrorCode;
import com.webank.ai.fate.serving.core.model.Model;
import com.webank.ai.fate.serving.core.model.ModelProcessor;
import com.webank.ai.fate.serving.core.rpc.core.FateService;
import com.webank.ai.fate.serving.core.rpc.core.FederatedRpcInvoker;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@FateService(name = "batchInferenece", preChain = {
      //  "monitorInterceptor",
        "requestOverloadBreaker",
        "guestBatchParamInterceptor",
        "guestModelInterceptor",
        "federationRouterInterceptor"
}, postChain = {
       // "monitorInterceptor"

})
@Service
public class GuestBatchInferenceProvider extends AbstractServingServiceProvider<BatchInferenceRequest, BatchInferenceResult> implements InitializingBean {

    final long DEFAULT_TIME_OUT = 3000;
    long timeout = DEFAULT_TIME_OUT;

    @Autowired
    FederatedRpcInvoker federatedRpcInvoker;

    @Autowired
    Environment environment;

    @Override
    public BatchInferenceResult doService(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) {

        Model model = ((ServingServerContext) context).getModel();
        ModelProcessor modelProcessor = model.getModelProcessor();
        BatchInferenceRequest batchInferenceRequest = (BatchInferenceRequest) inboundPackage.getBody();
        modelProcessor.guestPrepareDataBeforeInference(context, batchInferenceRequest);
        Map futureMap = Maps.newHashMap();

        Boolean useCache = environment.getProperty(Dict.PROPERTY_REMOTE_MODEL_INFERENCE_RESULT_CACHE_SWITCH, boolean.class, true);

        model.getFederationModelMap().forEach((hostPartyId, remoteModel) -> {
            BatchHostFederatedParams batchHostFederatedParams = buildBatchHostFederatedParams(context, batchInferenceRequest, model, remoteModel);
            //  ListenableFuture<Proxy.Packet> originBatchResultFuture = federatedRpcInvoker.async(context,  buildRpcDataWraper(model,remoteModel,batchHostFederatedParams));
            ListenableFuture<BatchInferenceResult> originBatchResultFuture = federatedRpcInvoker.batchInferenceRpcWithCache(context, buildRpcDataWraper(model, remoteModel, batchHostFederatedParams), useCache);
            futureMap.put(hostPartyId, originBatchResultFuture);
        });
        BatchInferenceResult batchFederatedResult = modelProcessor.guestBatchInference(context, batchInferenceRequest, futureMap, timeout);
        batchFederatedResult.setCaseid(context.getCaseId());
        return batchFederatedResult;
    }

    @Override
    protected OutboundPackage<BatchInferenceResult> serviceFailInner(Context context, InboundPackage<BatchInferenceRequest> data, Throwable e) {

        Map result = new HashMap();
        OutboundPackage<BatchInferenceResult> outboundPackage = new OutboundPackage<BatchInferenceResult>();
        BatchInferenceResult batchInferenceResult = new BatchInferenceResult();
        if (e instanceof BaseException) {
            BaseException baseException = (BaseException) e;
            batchInferenceResult.setRetcode(baseException.getRetcode());
            batchInferenceResult.setRetmsg(e.getMessage());
        } else {
            batchInferenceResult.setRetcode(ErrorCode.SYSTEM_ERROR);
        }
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

    @Override
    public void afterPropertiesSet() throws Exception {
        timeout = environment.getProperty(Dict.BATCH_PRC_TIMEOUT, Long.class, DEFAULT_TIME_OUT);
    }
}
