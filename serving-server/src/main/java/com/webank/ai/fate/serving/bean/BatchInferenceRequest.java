package com.webank.ai.fate.serving.bean;


import com.alibaba.fastjson.JSON;
import com.webank.ai.fate.serving.utils.InferenceUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BatchInferenceRequest {

    public  class  SingleInferenceData{

        String  caseId;
        Map<String,Object>  featureData;

        public String getCaseId() {
            return caseId;
        }

        public void setCaseId(String caseId) {
            this.caseId = caseId;
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
        Map<String,Object>  sendToRemoteFeatureData;
    }

    private List<SingleInferenceData> dataList;

    private String seqNo;

    public List<SingleInferenceData> getDataList() {
        return dataList;
    }

    public void setDataList(List<SingleInferenceData> dataList) {
        this.dataList = dataList;
    }

    public String getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(String seqNo) {
        this.seqNo = seqNo;
    }

    private String serviceId;
    private String applyId;
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

    @Override
    public String toString() {
        String result = "";
        try {
            result= JSON.toJSONString(this);
        } catch (Throwable e) {

        }
        return result;
    }


}
