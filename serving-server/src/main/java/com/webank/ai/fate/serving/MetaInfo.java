package com.webank.ai.fate.serving;

import com.webank.ai.fate.serving.core.adaptor.AdaptorDescriptor;

import java.util.List;

public class MetaInfo {

    static public long currentVersion = 200;

    static List<AdaptorDescriptor.ParamDescriptor> inferenceParamDescriptorList;

    static List<AdaptorDescriptor.ParamDescriptor> batchInferenceParamDescriptorList;


}
