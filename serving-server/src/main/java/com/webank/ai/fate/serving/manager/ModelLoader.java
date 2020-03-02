package com.webank.ai.fate.serving.manager;

import com.webank.ai.fate.serving.core.bean.ModelPipeline;
import com.webank.ai.fate.serving.core.bean.ModelProcessor;
import com.webank.ai.fate.serving.federatedml.PipelineTask;
import com.webank.ai.fate.serving.core.bean.Context;
public interface ModelLoader {

    public ModelProcessor loadModel(Context  context, String name, String namespace);

}
