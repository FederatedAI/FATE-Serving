package com.webank.ai.fate.serving.core.bean;


import java.util.List;
import java.util.Map;

public class BatchInferenceResult {

    String  retcode;

    List<SingleInferenceResult> dataList;


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

    }

    public String getRetcode() {
        return retcode;
    }

    public void setRetcode(String retcode) {
        this.retcode = retcode;
    }

    public List<SingleInferenceResult> getDataList() {
        return dataList;
    }

    public void setDataList(List<SingleInferenceResult> dataList) {
        this.dataList = dataList;
    }
}
