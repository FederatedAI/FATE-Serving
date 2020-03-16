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

package com.webank.ai.fate.serving.bean;

import com.alibaba.fastjson.JSON;
import com.webank.ai.fate.serving.core.bean.Request;
import com.webank.ai.fate.serving.utils.InferenceUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class InferenceRequest implements Request {
    private String appid;
    private String partyId;
    private String modelVersion;
    private String modelId;
    private String seqno;
    private String caseid;
    private String serviceId;
    private Map<String, Object> featureData;
    private String applyId;
    private Map<String, Object> sendToRemoteFeatureData;

    InferenceRequest() {
        seqno = InferenceUtils.generateSeqno();
        caseid = InferenceUtils.generateCaseid();
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

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public Map<String, Object> getSendToRemoteFeatureData() {
        return sendToRemoteFeatureData;
    }

    public void setSendToRemoteFeatureData(Map<String, Object> sendToRemoteFeatureData) {
        this.sendToRemoteFeatureData = sendToRemoteFeatureData;
    }

    public void setCaseId(String caseId) {
        this.caseid = caseId;
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
        return caseid;
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

    @Override
    public String getModelId() {
        return modelId;
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
