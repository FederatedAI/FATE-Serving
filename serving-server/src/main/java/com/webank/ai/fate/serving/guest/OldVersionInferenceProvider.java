package com.webank.ai.fate.serving.guest;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.serving.bean.InferenceRequest;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.constant.InferenceRetCode;
import com.webank.ai.fate.serving.core.model.Model;
import com.webank.ai.fate.serving.core.model.ModelProcessor;
import com.webank.ai.fate.serving.core.rpc.core.AbstractServiceAdaptor;
import com.webank.ai.fate.serving.core.rpc.core.FateService;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.rpc.FederatedRpcInvoker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *  主要兼容host为1.2.x版本的接口
 *
 **/
@FateService(name ="singleInference",  preChain= {
        "guestSingleParamInterceptor",
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


        InferenceRequest inferenceRequest = (InferenceRequest) inboundPackage.getBody();
//        BatchInferenceRequest batchInferenceRequest = (BatchInferenceRequest)inboundPackage.getBody();
        BatchInferenceRequest batchInferenceRequest = convertToBatchInferenceRequest(context, inferenceRequest);

        /**
         *  发往对端的参数
         */
//        BatchHostFederatedParams  batchHostFederatedParams = buildBatchHostFederatedParams( context,batchInferenceRequest);
        HostFederatedParams hostFederatedParams = buildHostFederatedParams(context, inferenceRequest);


        //==========
        /**
         * guest 端与host同步预测，再合并结果
         */

        ListenableFuture<Proxy.Packet> future = federatedRpcInvoker.async(context, hostFederatedParams.getPartnerLocal(), hostFederatedParams.getLocal(), hostFederatedParams, Dict.FEDERATED_INFERENCE);

        BatchInferenceResult batchFederatedResult = modelProcessor.guestBatchInference(context, batchInferenceRequest, future);

        ReturnResult returnResult = new ReturnResult();

        if (batchFederatedResult.getRetcode() == InferenceRetCode.OK) {
            List<BatchInferenceResult.SingleInferenceResult> dataList = batchFederatedResult.getDataList();
            if (dataList != null && dataList.size() > 0) {
                returnResult.setRetcode(dataList.get(0).getRetcode());
                returnResult.setRetmsg(dataList.get(0).getMsg());
                returnResult.setData(dataList.get(0).getData());
            }
        } else {
            returnResult.setRetcode(InferenceRetCode.GET_FEATURE_FAILED);
        }

        return returnResult;
    }

    private BatchInferenceRequest convertToBatchInferenceRequest(Context context, InferenceRequest inferenceRequest) {
        BatchInferenceRequest batchInferenceRequest = new BatchInferenceRequest();
        batchInferenceRequest.setServiceId(context.getServiceId());
        batchInferenceRequest.setApplyId(context.getApplyId());
        batchInferenceRequest.setCaseId(inferenceRequest.getCaseid());
        batchInferenceRequest.setSeqNo(inferenceRequest.getSeqno());

        List<BatchInferenceRequest.SingleInferenceData> dataList = new ArrayList<>();
        BatchInferenceRequest.SingleInferenceData data = new BatchInferenceRequest.SingleInferenceData();
        data.setIndex(0);
        data.setFeatureData(inferenceRequest.getFeatureData());
        data.setSendToRemoteFeatureData(inferenceRequest.getSendToRemoteFeatureData());
        dataList.add(data);

        batchInferenceRequest.setDataList(dataList);

        return batchInferenceRequest;
    }

    private HostFederatedParams buildHostFederatedParams(Context context, InferenceRequest inferenceRequest) {
        Model model = context.getModel();
        HostFederatedParams hostFederatedParams = new HostFederatedParams();
        hostFederatedParams.setCaseId(inferenceRequest.getCaseid());
        hostFederatedParams.setSeqNo(inferenceRequest.getSeqno());

        if (inferenceRequest.getSendToRemoteFeatureData() != null && hostFederatedParams.getFeatureIdMap() != null) {
            hostFederatedParams.getFeatureIdMap().putAll(inferenceRequest.getSendToRemoteFeatureData());
        }

        if (inferenceRequest.getFeatureData() != null && hostFederatedParams.getFeatureIdMap() != null) {
            hostFederatedParams.getFeatureIdMap().putAll(inferenceRequest.getFeatureData());
        }

        hostFederatedParams.setLocal(new FederatedParty(model.getFederationModel().getRole(), model.getFederationModel().getPartId()));
        hostFederatedParams.setPartnerLocal(new FederatedParty(model.getRole(), model.getPartId()));

        FederatedRoles federatedRoles = new FederatedRoles();

        List<String> guestPartyIds = new ArrayList<>();
        guestPartyIds.add(model.getPartId());
        federatedRoles.setRole(model.getRole(), guestPartyIds);
        List<String> hostPartyIds = new ArrayList<>();
        hostPartyIds.add(model.getFederationModel().getPartId());
        federatedRoles.setRole(model.getFederationModel().getRole(), hostPartyIds);
        hostFederatedParams.setRole(federatedRoles);

        hostFederatedParams.setPartnerModelInfo(new ModelInfo(model.getTableName(), model.getNamespace()));

//        hostFederatedParams.setData(guestFederatedParams.getData());
        return hostFederatedParams;
    }


    @Override
    protected ReturnResult transformErrorMap(Context context, Map data) {
        return null;
    }
}
