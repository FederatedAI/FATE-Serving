package com.webank.ai.fate.serving.core.bean;

import java.util.List;
import java.util.Map;

public class BatchInferenceRequest {

    public List<SingleInferenceData> getDataList() {
        return dataList;
    }

    public void setDataList(List<SingleInferenceData> dataList) {
        this.dataList = dataList;
    }

    List<SingleInferenceData> dataList;

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    String  serviceId;

    public class SingleInferenceData{

        private String caseId;

        public Map<String, Object> getFeatureData() {
            return featureData;
        }

        public void setFeatureData(Map<String, Object> featureData) {
            this.featureData = featureData;
        }

        private Map<String,Object>  featureData;

        public String getCaseId() {
            return caseId;
        }

        public void setCaseId(String caseId) {
            this.caseId = caseId;
        }


        public Map<String, Object> getSendToRemoteData() {
            return sendToRemoteData;
        }

        public void setSendToRemoteData(Map<String, Object> sendToRemoteData) {
            this.sendToRemoteData = sendToRemoteData;
        }

        private Map<String,Object>  sendToRemoteData;

    }


}
