package com.webank.ai.fate.serving.core.bean;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BatchInferenceResult {


    Integer version;

    String  retcode;

    String  msg;

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

    public String getRetcode() {
        return retcode;
    }

    public void setRetcode(String retcode) {
        this.retcode = retcode;
    }

    public List<SingleInferenceResult> getDataList() {
        if (dataList == null) {
            dataList = new ArrayList<>();
        }
        return dataList;
    }
    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }



    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }8




    public void setDataList(List<SingleInferenceResult> dataList) {
        this.dataList = dataList;
    }
}
