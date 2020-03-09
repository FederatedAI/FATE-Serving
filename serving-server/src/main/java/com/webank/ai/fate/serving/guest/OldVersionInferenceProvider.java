package com.webank.ai.fate.serving.guest;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.serving.bean.InferenceRequest;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.model.Model;
import com.webank.ai.fate.serving.core.model.ModelProcessor;
import com.webank.ai.fate.serving.core.rpc.core.AbstractServiceAdaptor;
import com.webank.ai.fate.serving.core.rpc.core.FateService;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.rpc.FederatedRpcInvoker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 *  主要兼容host为1.2.x版本的接口
 *
 **/
@FateService(name ="singleInference",  preChain= {
        "guestBatchParamInterceptor",
        "guestModelInterceptor",
        "federationRouterInterceptor"
      },postChain = {
        "defaultPostProcess"
})
@Service
public class OldVersionInferenceProvider extends AbstractServiceAdaptor<InferenceRequest,ReturnResult>{


    @Autowired
    FederatedRpcInvoker federatedRpcInvoker;

    @Override
    public ReturnResult doService(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) {

        Model  model = context.getModel();

        Preconditions.checkArgument(model!=null);
        /**
         * 用于替代原来的pipelineTask
         */
        ModelProcessor modelProcessor = model.getModelProcessor();


        InferenceRequest   inferenceRequest =(InferenceRequest)inboundPackage.getBody();


        /**
         *  发往对端的参数
         */
        BatchHostFederatedParams  batchHostFederatedParams = buildBatchHostFederatedParams( context,null);

        //==========
        /**
         * guest 端与host同步预测，再合并结果
         */

       // federatedRpcInvoker.async();

        ListenableFuture<Proxy.Packet> originBatchResultFuture = federatedRpcInvoker.asyncBatch(context,batchHostFederatedParams);

        BatchInferenceResult batchFederatedResult = modelProcessor.guestBatchInference(context, null, originBatchResultFuture);

        return  null;
    }


    @Override
    protected ReturnResult transformErrorMap(Context context, Map data) {
        return null;
    }
}
