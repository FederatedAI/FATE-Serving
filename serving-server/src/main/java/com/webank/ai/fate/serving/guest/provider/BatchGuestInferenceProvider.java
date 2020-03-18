package com.webank.ai.fate.serving.guest.provider;

import com.google.common.util.concurrent.ListenableFuture;
import com.webank.ai.fate.api.networking.proxy.Proxy;

import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.exceptions.BaseException;
import com.webank.ai.fate.serving.core.exceptions.ErrorCode;
import com.webank.ai.fate.serving.core.model.Model;
import com.webank.ai.fate.serving.core.model.ModelProcessor;
import com.webank.ai.fate.serving.core.rpc.core.*;
import com.webank.ai.fate.serving.rpc.FederatedRpcInvoker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@FateService(name ="batchInferenece",  preChain= {
       // "overloadMonitor",
        "guestBatchParamInterceptor",
        "guestModelInterceptor",
        "federationRouterInterceptor"
      },postChain = {
        "cache",
        ""
})
@Service
public class BatchGuestInferenceProvider extends AbstractServingServiceProvider<BatchInferenceRequest,BatchInferenceResult>{

    @Autowired
    FederatedRpcInvoker federatedRpcInvoker;

    @Override
    public BatchInferenceResult doService(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) {

        Model  model = context.getModel();

        ModelProcessor modelProcessor = model.getModelProcessor();

        BatchInferenceRequest   batchInferenceRequest =(BatchInferenceRequest)inboundPackage.getBody();
        /**
         *  准备发往对端的参数
         */
        BatchHostFederatedParams  batchHostFederatedParams = buildBatchHostFederatedParams( context,batchInferenceRequest);
        /**
         *  有些算法模块如 SBT,需要特殊的处理，会添加数据到sendToRemote ，如果后续有算法模块需要同样的操作，可以实现PrepareRemoteable接口
         */
        modelProcessor.guestPrepareDataBeforeInference(context,batchInferenceRequest);
        /**
         * guest 端与host同步预测，再合并结果
         */
        ListenableFuture<Proxy.Packet> originBatchResultFuture = federatedRpcInvoker.asyncBatch(context,batchHostFederatedParams);

        BatchInferenceResult batchFederatedResult = modelProcessor.guestBatchInference(context, batchInferenceRequest, originBatchResultFuture);

        return  batchFederatedResult;
    }

    @Override
    protected  OutboundPackage<BatchInferenceResult>  serviceFailInner(Context context, InboundPackage<BatchInferenceRequest> data, Throwable e) throws Exception{

        Map result = new HashMap();
        OutboundPackage<BatchInferenceResult> outboundPackage = new OutboundPackage<BatchInferenceResult>();
        BatchInferenceResult  batchInferenceResult = new  BatchInferenceResult();
        if(e instanceof BaseException){
            BaseException  baseException = (BaseException) e;
            batchInferenceResult.setRetcode(baseException.getRetcode());
            batchInferenceResult.setMsg(e.getMessage());
        }else{
            batchInferenceResult.setRetcode(ErrorCode.SYSTEM_ERROR);
        }
        outboundPackage.setData(batchInferenceResult);
        return  outboundPackage;

    }

    @Override
    protected BatchInferenceResult transformErrorMap(Context context, Map data) {

            return null;
    }
}
