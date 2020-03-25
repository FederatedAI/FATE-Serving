package com.webank.ai.fate.serving.core.bean;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BatchInferenceResult extends  ReturnResult{

    List<SingleInferenceResult> batchDataList;

    static public  class  SingleInferenceResult  {

        Integer  index;

        String  retcode;

        String  msg;

        Map<String ,Object> data;

        public String getRetcode() {
            return retcode;
        }

        public void setRetcode(String retcode) {
            this.retcode = retcode;
        }

        public Map<String, Object> getData() {
            return data;
        }

        public void setData(Map<String, Object> data) {
            this.data = data;
        }

        public Integer getIndex() {
            return index;
        }

        public void setIndex(Integer index) {
            this.index = index;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public SingleInferenceResult() {
        }

        public SingleInferenceResult(Integer index, String retcode, String msg, Map<String, Object> data) {
            this.index = index;
            this.retcode = retcode;
            this.msg = msg;
            this.data = data;
        }
    }


    public List<SingleInferenceResult> getBatchDataList() {
        if (batchDataList == null) {
            batchDataList = new ArrayList<>();
        }
        return batchDataList;
    }


    public void setBatchDataList(List<SingleInferenceResult> batchDataList) {
        this.batchDataList = batchDataList;
    }



}
