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

import com.alibaba.fastjson.JSON;
import com.webank.ai.fate.serving.core.utils.InferenceUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class InferenceRequest implements Request {
    protected String appid;
    protected String partyId;
    protected String modelVersion;
    protected String modelId;
    protected String seqno;
    protected String caseId;
    protected String serviceId;
    protected Map<String, Object> featureData;
    protected String applyId;
    protected Map<String, Object> sendToRemoteFeatureData;

    public InferenceRequest() {
        seqno = InferenceUtils.generateSeqno();
        caseId = InferenceUtils.generateCaseid();
        featureData = new HashMap<>();
        sendToRemoteFeatureData = new HashMap<>();
    }

    public String getApplyId() {
        return applyId;
    }

    public void setApplyId(String applyId) {
        this.applyId = applyId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public Map<String, Object> getSendToRemoteFeatureData() {
        return sendToRemoteFeatureData;
    }

    public void setSendToRemoteFeatureData(Map<String, Object> sendToRemoteFeatureData) {
        this.sendToRemoteFeatureData = sendToRemoteFeatureData;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    @Override
    public String getSeqno() {
        return seqno;
    }

    @Override
    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
        this.partyId = appid;
    }

    @Override
    public String getCaseid() {
        return caseId;
    }

    @Override
    public String getPartyId() {
        return partyId;
    }

    public void setPartyId(String partyId) {
        this.partyId = partyId;
        this.appid = partyId;
    }

    @Override
    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    @Override
    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    @Override
    public Map<String, Object> getFeatureData() {
        return featureData;
    }

    public boolean haveAppId() {
        return (!StringUtils.isEmpty(appid) || !StringUtils.isEmpty(partyId));
    }

    @Override
    public String toString() {
        String result = "";
        try {
            result = JSON.toJSONString(this);
        } catch (Throwable e) {

        }
        return result;
    }

}
