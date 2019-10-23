/*
 * Copyright 2019 The FATE Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.ai.fate.serving.host;

import com.google.common.base.Preconditions;
import com.webank.ai.fate.core.bean.ReturnResult;
import com.webank.ai.fate.core.utils.Configuration;
import com.webank.ai.fate.serving.adapter.dataaccess.FeatureData;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.constant.InferenceRetCode;
import com.webank.ai.fate.serving.federatedml.PipelineTask;
import com.webank.ai.fate.serving.federatedml.model.BaseModel;
import com.webank.ai.fate.serving.federatedml.model.HeteroSecureBoostingTreeHost;
import com.webank.ai.fate.serving.interfaces.ModelManager;
import com.webank.ai.fate.serving.utils.InferenceUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description TODO
 * @Author kaideng
 **/
@Service
public class DefaultHostInferenceProvider implements HostInferenceProvider {

    private static final Logger LOGGER = LogManager.getLogger(DefaultHostInferenceProvider.class);

    //Logger logger =  LoggerFactory.getLogger(DefaultHostInferenceProvider.class);
    @Autowired
    ModelManager modelManager;

    private static ReturnResult getFeatureData(Map<String, Object> featureIds) {
        ReturnResult defaultReturnResult = new ReturnResult();
        String classPath = FeatureData.class.getPackage().getName() + "." + Configuration.getProperty(Dict.PROPERTY_ONLINE_DATA_ACCESS_ADAPTER);
        FeatureData featureData = (FeatureData) InferenceUtils.getClassByName(classPath);
        if (featureData == null) {
            defaultReturnResult.setRetcode(InferenceRetCode.ADAPTER_ERROR);
            return defaultReturnResult;
        }
        try {
            return featureData.getData(featureIds);
        } catch (Exception ex) {
            defaultReturnResult.setRetcode(InferenceRetCode.GET_FEATURE_FAILED);
            LOGGER.error("get feature data error:", ex);
            return defaultReturnResult;
        }
    }

    @Override
    public ReturnResult federatedInference(Context context, HostFederatedParams federatedParams) {


        long startTime = System.currentTimeMillis();
        ReturnResult returnResult = new ReturnResult();
        //TODO: Very ugly, need to be optimized
//        FederatedParty party = (FederatedParty) ObjectTransform.json2Bean(federatedParams.get("local").toString(), FederatedParty.class);
//        FederatedRoles federatedRoles = (FederatedRoles) ObjectTransform.json2Bean(federatedParams.get("role").toString(), FederatedRoles.class);
//        ModelInfo partnerModelInfo = (ModelInfo) ObjectTransform.json2Bean(federatedParams.get("partner_model_info").toString(), ModelInfo.class);
//        Map<String, Object> featureIds = (Map<String, Object>) ObjectTransform.json2Bean(federatedParams.get("feature_id").toString(), HashMap.class);
        boolean billing = false;
        FederatedParty party = federatedParams.getLocal();
        FederatedRoles federatedRoles = federatedParams.getRole();
        ModelInfo partnerModelInfo = federatedParams.getPartnerModelInfo();
        Map<String, Object> featureIds = federatedParams.getFeatureIdMap();

        ModelInfo modelInfo = modelManager.getModelInfoByPartner(partnerModelInfo.getName(), partnerModelInfo.getNamespace());
        if (modelInfo == null) {
            returnResult.setRetcode(InferenceRetCode.LOAD_MODEL_FAILED);
            returnResult.setRetmsg("Can not found model.");
            //   logInference(context,federatedParams, party, federatedRoles, returnResult, 0, false, false);
            return returnResult;
        }
        PipelineTask model = modelManager.getModel(modelInfo.getName(), modelInfo.getNamespace());
        if (model == null) {
            returnResult.setRetcode(InferenceRetCode.LOAD_MODEL_FAILED);
            returnResult.setRetmsg("Can not found model.");
            //     InferenceUtils.logInference(context ,federatedParams, party, federatedRoles, returnResult, 0, false, false);
            return returnResult;
        }
        LOGGER.info("use model to inference on {} {}, id: {}, version: {}", party.getRole(), party.getPartyId(), modelInfo.getNamespace(), modelInfo.getName());
        Map<String, Object> predictParams = new HashMap<>();
        predictParams.put(Dict.FEDERATED_PARAMS, federatedParams);

        try {
            ReturnResult getFeatureDataResult = getFeatureData(featureIds);
            if (getFeatureDataResult.getRetcode() == InferenceRetCode.OK) {
                if (getFeatureDataResult.getData() == null || getFeatureDataResult.getData().size() < 1) {
                    returnResult.setRetcode(InferenceRetCode.GET_FEATURE_FAILED);
                    returnResult.setRetmsg("Can not get feature data.");
                    //   InferenceUtils.logInference(context,federatedParams, party, federatedRoles, returnResult, 0, false, false);
                    return returnResult;
                }
                Map<String, Object> result = model.predict(context, getFeatureDataResult.getData(), federatedParams);
                returnResult.setRetcode(InferenceRetCode.OK);
                returnResult.setData(result);
                billing = true;
            } else {
                returnResult.setRetcode(getFeatureDataResult.getRetcode());
            }
        } catch (Exception ex) {
            LOGGER.info("federatedInference error:", ex);
            returnResult.setRetcode(InferenceRetCode.SYSTEM_ERROR);
            returnResult.setRetmsg(ex.getMessage());
        }
        long endTime = System.currentTimeMillis();
        long federatedInferenceElapsed = endTime - startTime;
        //   InferenceUtils.logInference(context ,federatedParams, party, federatedRoles, returnResult, federatedInferenceElapsed, false, billing);
        LOGGER.info(returnResult.getData());
        return returnResult;


    }

    @Override
    public ReturnResult federatedInferenceForTree(Context context, HostFederatedParams federatedParams) {

        // try {

        ReturnResult returnResult = new ReturnResult();
        FederatedParty party = federatedParams.getLocal();
        FederatedRoles federatedRoles = federatedParams.getRole();
        ModelInfo partnerModelInfo = federatedParams.getPartnerModelInfo();
        Map<String, Object> featureIds = federatedParams.getFeatureIdMap();

        ModelInfo modelInfo = modelManager.getModelInfoByPartner(partnerModelInfo.getName(), partnerModelInfo.getNamespace());
        if (modelInfo == null) {
            returnResult.setRetcode(InferenceRetCode.LOAD_MODEL_FAILED);
            returnResult.setRetmsg("Can not found model.");
            //   logInference(context,federatedParams, party, federatedRoles, returnResult, 0, false, false);
            return returnResult;
        }


        PipelineTask model = modelManager.getModel(modelInfo.getName(), modelInfo.getNamespace());

        //   Preconditions.checkArgument(federatedParams.getData().get(Dict.TAG)!=null);

        //   String  tag = federatedParams.getData().get(Dict.TAG).toString();

        Preconditions.checkArgument(federatedParams.getData().get(Dict.COMPONENT_NAME) != null);

        String componentName = federatedParams.getData().get(Dict.COMPONENT_NAME).toString();


        BaseModel baseModel = model.getModelByComponentName(componentName);

        Preconditions.checkArgument(baseModel != null);

        Map<String, Object> resultData = ((HeteroSecureBoostingTreeHost) baseModel).predictSingleRound(context, (Map<String, Object>) federatedParams.getData().get(Dict.TREE_LOCATION), federatedParams);

        returnResult.setRetcode(InferenceRetCode.OK);
        returnResult.setData(resultData);

        return returnResult;
//        }catch(Throwable  e){
//
//
//
//        }


    }
}
