package com.webank.ai.fate.serving.adapter.processing;


import com.alibaba.fastjson.JSON;
import com.google.gson.annotations.JsonAdapter;
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
        preProcessingResult.setProcessingResult(JSON.parseObject(paras, HashMap.class));
        preProcessingResult.setFeatureIds(preProcessingResult.getProcessingResult());
        return preProcessingResult;
    }

}
