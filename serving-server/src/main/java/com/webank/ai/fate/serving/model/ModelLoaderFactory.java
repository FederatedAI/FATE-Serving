package com.webank.ai.fate.serving.model;


import com.webank.ai.fate.serving.core.bean.Context;

public interface ModelLoaderFactory {

    ModelLoader  getModelLoader(Context context, ModelLoader.LoadModelType loadModelType);
}
