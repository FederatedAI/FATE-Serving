package com.webank.ai.fate.serving.common.model;


import com.webank.ai.fate.serving.core.bean.Context;

import java.util.List;
import java.util.Map;

public interface MergeInferenceAware {

    public Map<String, Object> mergeRemoteInference(Context context, List<Map<String, Object>> localData,
                                                    Map<String, Object> remoteData);

}
