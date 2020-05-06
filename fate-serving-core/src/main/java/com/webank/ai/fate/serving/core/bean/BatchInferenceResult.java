package com.webank.ai.fate.serving.core.bean;


import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BatchInferenceResult extends  ReturnResult{

    List<SingleInferenceResult> batchDataList;

    static public  class  SingleInferenceResult  {

        Integer  index;

        String  retcode;


        String  retmsg;

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
        public String getRetmsg() {
            return retmsg;
        }

        public void setRetmsg(String retmsg) {
            this.retmsg = retmsg;
        }


        public SingleInferenceResult() {
        }

        public SingleInferenceResult(Integer index, String retcode, String msg, Map<String, Object> data) {
            this.index = index;
            this.retcode = retcode;
            this.retmsg = msg;
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

    private  Map<Integer,SingleInferenceResult>  singleInferenceResultMap ;


    public void  rebuild(){



            Map result  =  Maps.newHashMap();

            List<BatchInferenceResult.SingleInferenceResult>  batchInferences = this.getBatchDataList();

            for(BatchInferenceResult.SingleInferenceResult  singleInferenceResult:batchInferences){

                result.put(singleInferenceResult.getIndex(),singleInferenceResult);
            }
            singleInferenceResultMap =result;

    }


    public Map<Integer,SingleInferenceResult>   getSingleInferenceResultMap(){


        if(singleInferenceResultMap==null){

                rebuild();
        }

        return singleInferenceResultMap;




    };



}
