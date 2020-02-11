package com.webank.ai.fate.serving.manger;

import com.webank.ai.fate.serving.federatedml.PipelineTask;
import com.webank.ai.fate.serving.core.bean.Context;
public interface ModelLoader {

    public PipelineTask loadModel(Context  context,String name, String namespace);

}
