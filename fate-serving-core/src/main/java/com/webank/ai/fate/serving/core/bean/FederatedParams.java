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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;
import java.util.Map;

public class FederatedParams {

    String caseId;
    String seqNo;
    boolean isBatch;
    FederatedParty local;
    ModelInfo modelInfo;
    FederatedRoles role;
    Map<String, Object> featureIdMap = Maps.newHashMap();
    List<Map<String, Object>> batchFeatureIdMapList = Lists.newArrayList();
    Map<String, Object> data = Maps.newHashMap();

    public boolean isBatch() {
        return isBatch;
    }

    public void setBatch(boolean batch) {
        isBatch = batch;
    }

    public List<Map<String, Object>> getBatchFeatureIdMapList() {
        return batchFeatureIdMapList;
    }

    public void setBatchFeatureIdMapList(List<Map<String, Object>> batchFeatureIdMapList) {
        this.batchFeatureIdMapList = batchFeatureIdMapList;
    }

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
