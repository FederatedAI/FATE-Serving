package com.webank.ai.fate.serving.core.bean;


import com.alibaba.fastjson.JSON;

import java.util.List;
import java.util.Map;

public class BatchInferenceRequest {

    public static class SingleInferenceData {
        String caseId;
        int index;
        Map<String, Object> featureData;
        Map<String, Object> sendToRemoteFeatureData;

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

        public String getCaseId() {
            return caseId;
        }

        public void setCaseId(String caseId) {
            this.caseId = caseId;
        }
    }

    private List<SingleInferenceData> dataList;

    public List<SingleInferenceData> getDataList() {
        return dataList;
    }

    public void setDataList(List<SingleInferenceData> dataList) {
        this.dataList = dataList;
    }

    private String serviceId;
    private String applyId;
    private String seqNo;

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

    public String getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(String seqNo) {
        this.seqNo = seqNo;
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
