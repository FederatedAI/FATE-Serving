package com.webank.ai.fate.serving.adapter.processing;


import com.webank.ai.fate.serving.bean.PreProcessingResult;
import com.webank.ai.fate.serving.core.bean.Context;
import jdk.nashorn.internal.runtime.ParserException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CommonPreProcessing implements PreProcessing {
    @Override
    public PreProcessingResult getResult(Context context , String paras) {
        PreProcessingResult preProcessingResult = new PreProcessingResult();
        preProcessingResult.setProcessingResult(preProcessing(paras));
        Map<String, Object> featureIds = new HashMap<>();
        JSONObject paraObj = new JSONObject(paras);
        preProcessingResult.setFeatureIds(featureIds);
        return preProcessingResult;
    }

    private Map<String, Object> preProcessing(String paras) throws ClassCastException, ParserException {
        Map<String, Object> feature = new HashMap<>();

        return feature;
    }

    public static  void main(String[] args){
    }
}
