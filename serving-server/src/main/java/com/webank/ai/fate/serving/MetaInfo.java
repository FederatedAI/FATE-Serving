package com.webank.ai.fate.serving;

import com.webank.ai.fate.serving.adapter.dataaccess.AdaptorDescriptor;

import java.util.List;

public class MetaInfo {

    static long currentVersion = 200;

    static List<AdaptorDescriptor.ParamDescriptor> inferenceParamDescriptorList;

    static List<AdaptorDescriptor.ParamDescriptor> batchInferenceParamDescriptorList;


}
