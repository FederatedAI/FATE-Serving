package com.webank.ai.fate.serving.core.bean;


import java.util.Map;

public interface MergeInferenceAware {

    public Map<String,Object> mergeRemoteInference(Context context, Map<String,Object> input);

}
