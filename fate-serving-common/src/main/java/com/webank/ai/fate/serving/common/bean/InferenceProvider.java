package com.webank.ai.fate.serving.common.bean;

import com.webank.ai.fate.api.serving.InferenceServiceProto;
import com.webank.ai.fate.serving.core.bean.Context;

public interface InferenceProvider {

    InferenceServiceProto.InferenceMessage inference(Context context, InferenceServiceProto.InferenceMessage req);
}
