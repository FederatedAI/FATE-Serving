package com.webank.ai.fate.serving.adapter.dataaccess;


import java.util.Map;

public class BatchFeatureResult {

    String  caseId;
    public  static class   SingleFeatureResult{
        int  retcode;
        String index;
        Map<String,Object>  data;
    }
}
