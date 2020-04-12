package com.webank.ai.fate.serving.core.bean;


import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public class BatchInferenceRequest extends InferenceRequest{

    public static class SingleInferenceData {
        int index;
        Map<String, Object> featureData= Maps.newHashMap();
        Map<String, Object> sendToRemoteFeatureData =Maps.newHashMap();

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public Map<String, Object> getFeatureData() {
            return featureData;
        }

        public void setFeatureData(Map<String, Object> featureData) {
            this.featureData = featureData;
        }

        public Map<String, Object> getSendToRemoteFeatureData() {
            return sendToRemoteFeatureData;
        }

        public void setSendToRemoteFeatureData(Map<String, Object> sendToRemoteFeatureData) {
            this.sendToRemoteFeatureData = sendToRemoteFeatureData;
        }
    }

    private String serviceId;


    private List<SingleInferenceData> batchDataList;

    public List<SingleInferenceData> getBatchDataList() {
        return batchDataList;
    }

    public void setBatchDataList(List<SingleInferenceData> batchDataList) {
        this.batchDataList = batchDataList;
    }



    public String getCaseId() {
        return caseId;
    }

    @Override
    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    @Override
    public String getApplyId() {
        return applyId;
    }

    @Override
    public void setApplyId(String applyId) {
        this.applyId = applyId;
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }

    @Override
    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
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
