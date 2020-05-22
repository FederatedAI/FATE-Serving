package com.webank.ai.fate.serving.adapter.processing;


import com.alibaba.fastjson.JSON;
import com.webank.ai.fate.serving.bean.PreProcessingResult;
import com.webank.ai.fate.serving.core.bean.Context;

import java.util.HashMap;

public class CommonPreProcessing implements PreProcessing {
    @Override
    public PreProcessingResult getResult(Context context , String paras) {
        PreProcessingResult preProcessingResult = new PreProcessingResult();
        preProcessingResult.setProcessingResult(JSON.parseObject(paras, HashMap.class));
        return preProcessingResult;
    }

}
