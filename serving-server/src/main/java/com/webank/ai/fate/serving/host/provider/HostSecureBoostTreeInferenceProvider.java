package com.webank.ai.fate.serving.host.provider;

import com.google.common.base.Preconditions;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.model.Model;
import com.webank.ai.fate.serving.core.rpc.core.AbstractServiceAdaptor;
import com.webank.ai.fate.serving.core.rpc.core.FateService;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.federatedml.model.HeteroSecureBoostingTreeHost;
import com.webank.ai.fate.serving.guest.provider.AbstractServingServiceProvider;
import com.webank.ai.fate.serving.model.ModelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
@FateService(name ="batchInferenece",  preChain= {
//        "overloadMonitor",
        "hostModelInterceptor"
},postChain = {
        "defaultPostProcess"
})
@Service
public class HostSecureBoostTreeInferenceProvider  extends AbstractServingServiceProvider {

    private static final Logger logger = LoggerFactory.getLogger(BatchHostInferenceProvider.class);

//    @Override
//    public ReturnResult federatedInference(Context context, BatchHostFederatedParams batchHostFederatedParams) {
//
//
//        String tableName = batchHostFederatedParams.getHostTableName();
//        String namespace = batchHostFederatedParams.getHostNamespace();
//
//
//
//        long startTime = System.currentTimeMillis();
//        ReturnResult returnResult = new ReturnResult();
//        boolean billing = false;
//        FederatedParty party = federatedParams.getLocal();
//        FederatedRoles federatedRoles = federatedParams.getRole();
//        ModelInfo partnerModelInfo = federatedParams.getPartnerModelInfo();
//        Map<String, Object> featureIds = federatedParams.getFeatureIdMap();
//
//        ModelInfo modelInfo = modelManager.getModelInfoByPartner(context,partnerModelInfo.getName(), partnerModelInfo.getNamespace());
//        if (modelInfo == null) {
//            returnResult.setRetcode(InferenceRetCode.LOAD_MODEL_FAILED);
//            returnResult.setRetmsg("Can not found model.");
//            //   logInference(context,federatedParams, party, federatedRoles, returnResult, 0, false, false);
//            return returnResult;
//        }
//        PipelineTask model = modelManager.getModel(context,modelInfo.getName(), modelInfo.getNamespace());
//        if (model == null) {
//            returnResult.setRetcode(InferenceRetCode.LOAD_MODEL_FAILED);
//            returnResult.setRetmsg("Can not found model.");
//            return returnResult;
//        }
//
//        logger.info("use model to inference on {} {}, id: {}, version: {}", party.getRole(), party.getPartyId(), modelInfo.getNamespace(), modelInfo.getName());
//
//        Map<String, Object> predictParams = new HashMap<>(8);
//        predictParams.put(Dict.FEDERATED_PARAMS, federatedParams);
//
//        try {
//            ReturnResult getFeatureDataResult = getFeatureData(context,featureIds);
//            if (getFeatureDataResult.getRetcode() == InferenceRetCode.OK) {
//                if (getFeatureDataResult.getData() == null || getFeatureDataResult.getData().size() < 1) {
//                    returnResult.setRetcode(InferenceRetCode.GET_FEATURE_FAILED);
//                    returnResult.setRetmsg("Can not get feature data.");
//                    //   InferenceUtils.logInference(context,federatedParams, party, federatedRoles, returnResult, 0, false, false);
//                    return returnResult;
//                }
//                Map<String, Object> result = model.predict(context, getFeatureDataResult.getData(), federatedParams);
//                returnResult.setRetcode(InferenceRetCode.OK);
//                returnResult.setData(result);
//                billing = true;
//            } else {
//                returnResult.setRetcode(getFeatureDataResult.getRetcode());
//            }
//        } catch (Exception ex) {
//            logger.info("federatedInference error:", ex);
//            returnResult.setRetcode(InferenceRetCode.SYSTEM_ERROR);
//            returnResult.setRetmsg(ex.getMessage());
//        }
//        long endTime = System.currentTimeMillis();
//        long federatedInferenceElapsed = endTime - startTime;
//        //   InferenceUtils.logInference(context ,federatedParams, party, federatedRoles, returnResult, federatedInferenceElapsed, false, billing);
//        if (logger.isDebugEnabled()) {
//            logger.debug(JSONObject.toJSONString(returnResult.getData()));
//        }
//        return returnResult;
//
//
//    }

//    @Override
//    public ReturnResult federatedInferenceForTree(Context context, HostFederatedParams federatedParams) {
//
//        // try {
//
//        ReturnResult returnResult = new ReturnResult();
//        FederatedParty party = federatedParams.getLocal();
//        FederatedRoles federatedRoles = federatedParams.getRole();
//        ModelInfo partnerModelInfo = federatedParams.getPartnerModelInfo();
//        Map<String, Object> featureIds = federatedParams.getFeatureIdMap();
//
//        ModelInfo modelInfo = modelManager.getModelInfoByPartner(context,partnerModelInfo.getName(), partnerModelInfo.getNamespace());
//        if (modelInfo == null) {
//            returnResult.setRetcode(InferenceRetCode.LOAD_MODEL_FAILED);
//            returnResult.setRetmsg("Can not found model.");
//            //   logInference(context,federatedParams, party, federatedRoles, returnResult, 0, false, false);
//            return returnResult;
//        }
//
//
//        PipelineTask model = modelManager.getModel(context,modelInfo.getName(), modelInfo.getNamespace());
//
//        //   Preconditions.checkArgument(federatedParams.getData().get(Dict.TAG)!=null);
//
//        //   String  tag = federatedParams.getData().get(Dict.TAG).toString();
//
//        Preconditions.checkArgument(federatedParams.getData().get(Dict.COMPONENT_NAME) != null);
//
//        String componentName = federatedParams.getData().get(Dict.COMPONENT_NAME).toString();
//
//
//        BaseModel baseModel = model.getModelByComponentName(componentName);
//
//        Preconditions.checkArgument(baseModel != null);
//
//        Map<String, Object> resultData = ((HeteroSecureBoostingTreeHost) baseModel).predictSingleRound(context, (Map<String, Object>) federatedParams.getData().get(Dict.TREE_LOCATION), federatedParams);
//
//        returnResult.setRetcode(InferenceRetCode.OK);
//        returnResult.setData(resultData);
//
//        return returnResult;
////        }catch(Throwable  e){
////
////
////
////        }
//
//
//    }

    @Override
    public Object doService(Context context, InboundPackage data, OutboundPackage outboundPackage) {

        Map params = (Map) data.getBody();

        Model model = ((ServingServerContext)context).getModel();

        Object componentObject = model.getModelProcessor().getComponent(Dict.COMPONENT_NAME);

        Preconditions.checkArgument(componentObject!=null);

        HeteroSecureBoostingTreeHost  heteroSecureBoostingTreeHost = (HeteroSecureBoostingTreeHost)componentObject;

        Map<String, Object> map = heteroSecureBoostingTreeHost.predictSingleRound(context,params);

        ReturnResult  result = new  ReturnResult();

        result.setRetcode(StatusCode.SUCCESS);

        result.setData(map);

        return   result;
    }


}