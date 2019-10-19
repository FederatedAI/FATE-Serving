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

package com.webank.ai.fate.serving.core.bean;


import com.google.common.collect.Maps;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Map;

/**
 * @Description TODO
 * @Author kaideng
 **/
public class FederatedParams {


//            federatedParams.put(Dict.CASEID, inferenceRequest.getCaseid());
//        federatedParams.put(Dict.SEQNO, inferenceRequest.getSeqno());
//        federatedParams.put("local", modelNamespaceData.getLocal());
//        federatedParams.put("model_info", new ModelInfo(modelName, modelNamespace));
//        federatedParams.put("role", modelNamespaceData.getRole());
//        federatedParams.put("feature_id", featureIds);

    String caseId;

    String seqNo;

    FederatedParty local;
    ModelInfo modelInfo;
    FederatedRoles role;
    Map<String, Object> featureIdMap;
    Map<String, Object> data = Maps.newHashMap();

    public ModelInfo getModelInfo() {
        return modelInfo;
    }

    public void setModelInfo(ModelInfo modelInfo) {
        this.modelInfo = modelInfo;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(String seqNo) {
        this.seqNo = seqNo;
    }

    public FederatedParty getLocal() {
        return local;
    }

    public void setLocal(FederatedParty local) {
        this.local = local;
    }

    public FederatedRoles getRole() {
        return role;
    }

    public void setRole(FederatedRoles role) {
        this.role = role;
    }

    public Map<String, Object> getFeatureIdMap() {
        return featureIdMap;
    }

    public void setFeatureIdMap(Map<String, Object> featureIdMap) {
        this.featureIdMap = featureIdMap;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    @Override
    public String toString() {

        return ToStringBuilder.reflectionToString(this);
    }


}
