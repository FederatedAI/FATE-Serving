package com.webank.ai.fate.serving.guest;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import com.webank.ai.fate.api.networking.proxy.Proxy;

import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.bean.ModelInfo;
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
 * @Description TODO
 * @Author kaideng
 **/
@FateService(name ="batchInferenece",  preChain= {
//        "overloadMonitor",
//        "batchParamInterceptor",
        "guestBatchParamInterceptor",
//        "federationModelInterceptor",
        "guestModelInterceptor",
//        "federationRouterService"
        "federationRouterInterceptor"
      },postChain = {
        "defaultPostProcess"
})
@Service
public class BatchGuestInferenceProvider extends AbstractServiceAdaptor<BatchInferenceRequest,BatchInferenceResult>{


    @Autowired
    FederatedRpcInvoker federatedRpcInvoker;


    private  BatchHostFederatedParams  buildBatchHostFederatedParams(Context  context,BatchInferenceRequest  batchInferenceRequest){
        Model model = context.getModel();
        Model hostModel  = model.getFederationModel();
        BatchHostFederatedParams  batchHostFederatedParams = new  BatchHostFederatedParams();
        String seqNo = batchInferenceRequest.getSeqNo();
        batchHostFederatedParams.setGuestPartyId(model.getPartId());
        batchHostFederatedParams.setHostPartyId(model.getFederationModel().getPartId());
        List<BatchHostFederatedParams.SingleInferenceData> sendToHostDataList= Lists.newArrayList();

        List<BatchInferenceRequest.SingleInferenceData> guestDataList = batchInferenceRequest.getDataList();
        for(BatchInferenceRequest.SingleInferenceData  singleInferenceData:guestDataList) {
            BatchHostFederatedParams.SingleInferenceData singleBatchHostFederatedParam = new BatchHostFederatedParams.SingleInferenceData();
            singleBatchHostFederatedParam.setCaseId(singleInferenceData.getCaseId());
            singleBatchHostFederatedParam.setSendToRemoteFeatureData(singleInferenceData.getSendToRemoteFeatureData());
            sendToHostDataList.add(singleBatchHostFederatedParam);
        }
        batchHostFederatedParams.setDataList(sendToHostDataList);
        batchHostFederatedParams.setHostTableName(hostModel.getTableName());
        batchHostFederatedParams.setHostNamespace(hostModel.getNamespace());
        batchHostFederatedParams.setSeqNo(seqNo);






//        ModelInfo modelInfo =new ModelInfo(model.getTableName(), model.getNamespace());
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
//        hostFederatedParams.setData(guestFederatedParams.getData());

        return  batchHostFederatedParams;

    }

    @Override
    public BatchInferenceResult doService(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) {

        Model  model = context.getModel();

        Preconditions.checkArgument(model!=null);
        /**
         * 用于替代原来的pipelineTask
         */
        ModelProcessor modelProcessor = model.getModelProcessor();


        BatchInferenceRequest   batchInferenceRequest =(BatchInferenceRequest)inboundPackage.getBody();
        /**
         *  发往对端的参数
         */
        BatchHostFederatedParams  batchHostFederatedParams = buildBatchHostFederatedParams( context,batchInferenceRequest);

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

        ListenableFuture<Proxy.Packet> originBatchResultFuture = federatedRpcInvoker.asyncBatch(context,batchHostFederatedParams);

//        BatchFederatedResult    batchFederatedResult = modelProcessor.batchPredict(context,batchInferenceRequest,originBatchResultFuture);
        BatchInferenceResult batchFederatedResult = modelProcessor.guestBatchInference(context, batchInferenceRequest, originBatchResultFuture);

        return  batchFederatedResult;
    }

//    private  ReturnResult  buildReturnResult(Context  context,BatchFederatedResult batchFederatedResult){
//
//
//        return  null;
//    }


    @Override
    protected BatchInferenceResult transformErrorMap(Context context, Map data) {
        return null;
    }
}
