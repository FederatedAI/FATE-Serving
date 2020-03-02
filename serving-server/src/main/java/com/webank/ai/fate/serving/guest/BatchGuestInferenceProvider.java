package com.webank.ai.fate.serving.guest;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.serving.bean.BatchInferenceRequest;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.bean.ModelInfo;
import com.webank.ai.fate.serving.core.model.Model;
import com.webank.ai.fate.serving.core.rpc.core.AbstractServiceAdaptor;
import com.webank.ai.fate.serving.core.rpc.core.FateService;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.rpc.FederatedRpcInvoker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @Description TODO
 * @Author kaideng
 **/
@FateService(name ="batchInferenece",  preChain= {
//        "overloadMonitor",
        "batchParamInterceptor",
        "federationModelInterceptor",
        "federationRouterService"
      },postChain = {
        "defaultPostProcess"
})
@Service
public class BatchGuestInferenceProvider extends AbstractServiceAdaptor<BatchInferenceRequest,ReturnResult>{


    @Autowired
    FederatedRpcInvoker federatedRpcInvoker;


    private  BatchHostFederatedParams  buildBatchHostFederatedParams(){


        BatchHostFederatedParams  batchHostFederatedParams = new  BatchHostFederatedParams();

        return  batchHostFederatedParams;

    }

    @Override
    public ReturnResult doService(Context context, InboundPackage data, OutboundPackage outboundPackage) {



        Model  model = context.getModel();

        Preconditions.checkArgument(model!=null);
        /**
         * 用于替代原来的pipelineTask
         */
        ModelProcessor localModelProcessor = model.getModelProcessor();
        /**
         *  发往对端的参数
         */
        BatchHostFederatedParams  batchHostFederatedParams = buildBatchHostFederatedParams();


//        PipelineTask  pipelineTask = (PipelineTask) pipeLineObject;
//
//
//        ModelInfo modelInfo =new ModelInfo(modelName, modelNamespace);
//        HostFederatedParams hostFederatedParams = new HostFederatedParams();
//        hostFederatedParams.setCaseId(bat);
//        hostFederatedParams.setSeqNo(guestFederatedParams.getSeqNo());
//        // hostFederatedParams.getFeatureIdMap().putAll(guestFederatedParams.getFeatureIdMap());
//        hostFederatedParams.setBatchFeatureIdMapList();
//        hostFederatedParams.setBatch(true);
//        hostFederatedParams.setLocal(dstParty);
//        hostFederatedParams.setPartnerLocal(srcParty);
//        hostFederatedParams.setRole(modelNamespaceData.getRole());
//        hostFederatedParams.setPartnerModelInfo(modelInfo);
        // hostFederatedParams.setData(guestFederatedParams.getData());

        //==========
        /**
         * guest 端与host同步预测，再合并结果
         */

        ListenableFuture<Proxy.Packet> originbatchResultFuture = federatedRpcInvoker.asyncBatch(context,batchHostFederatedParams);

        BatchFederatedResult    batchFederatedResult = localModelProcessor.batchPredict(context,null,originbatchResultFuture);

        return buildReturnResult(context,batchFederatedResult);
    }

    private  ReturnResult  buildReturnResult(Context  context,BatchFederatedResult batchFederatedResult){


        return  null;
    }


    @Override
    protected ReturnResult transformErrorMap(Context context, Map data) {
        return null;
    }
}
