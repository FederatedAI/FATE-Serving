package com.webank.ai.fate.serving.core.bean;


import java.util.List;
import java.util.Map;

public class BatchHostFederatedParams {

    String  hostTableName;
    String  hostNamespace;
    String  guestPartyId;
    String  hostPartyId;
    String  seqNo;
    List<SingleBatchHostFederatedParam> dataList;

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

    public String getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(String seqNo) {
        this.seqNo = seqNo;
    }

    public List<SingleBatchHostFederatedParam> getDataList() {
        return dataList;
    }

    public void setDataList(List<SingleBatchHostFederatedParam> dataList) {
        this.dataList = dataList;
    }



    public static class  SingleBatchHostFederatedParam{

        String  caseId;

        Map<String,Object> sendToRemoteData;

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


    }



}
