package com.webank.ai.fate.serving.core.model;

import com.webank.ai.fate.serving.core.bean.Context;

import java.util.List;
import java.util.Map;

public interface LocalInferenceAware {

    public Map<String, Object> localInference(Context context, List<Map<String, Object>> input);

}
