package com.webank.ai.fate.serving.core.bean;


import java.util.List;
import java.util.Map;

public class BatchHostFederatedParams extends  BatchInferenceRequest{

    String  hostTableName;
    String  hostNamespace;
    String  guestPartyId;
    String  hostPartyId;
    String  caseId;

    public String getHostTableName() {
        return hostTableName;
    }

    public void setHostTableName(String hostTableName) {
        this.hostTableName = hostTableName;
    }

    public String getHostNamespace() {
        return hostNamespace;
    }

    public void setHostNamespace(String hostNamespace) {
        this.hostNamespace = hostNamespace;
    }

    public String getGuestPartyId() {
        return guestPartyId;
    }

    public void setGuestPartyId(String guestPartyId) {
        this.guestPartyId = guestPartyId;
    }

    public String getHostPartyId() {
        return hostPartyId;
    }

    public void setHostPartyId(String hostPartyId) {
        this.hostPartyId = hostPartyId;
    }

    @Override
    public String getCaseId() {
        return caseId;
    }

    @Override
    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }







//    public static class  SingleBatchHostFederatedParam{
//
//
//        String  index;
//
//        Map<String,Object> sendToRemoteData;
//
//        public Map<String, Object> getSendToRemoteData() {
//            return sendToRemoteData;
//        }
//
//        public void setSendToRemoteData(Map<String, Object> sendToRemoteData) {
//            this.sendToRemoteData = sendToRemoteData;
//        }
//        public String getIndex() {
//            return index;
//        }
//
//        public void setIndex(String index) {
//            this.index = index;
//        }
//
//
//    }



}
