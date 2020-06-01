package com.webank.ai.fate.serving.federatedml.model;


import com.webank.ai.fate.serving.core.bean.Context;

import java.util.Map;

public interface PrepareRemoteable {

    public Map<String, Object> prepareRemoteData(Context context, Map<String, Object> input);
}
