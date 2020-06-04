package com.webank.ai.fate.serving.model;


import com.webank.ai.fate.serving.core.bean.Context;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

@Service
public class SimpleModelLoaderFactory implements ModelLoaderFactory, ApplicationContextAware {

    ApplicationContext applicationContext;


    @Override
    public ModelLoader getModelLoader(Context context, ModelLoader.LoadModelType loadModelType) {


        switch (loadModelType.toString()) {

            case "FATEFLOW":
                return (ModelLoader) applicationContext.getBean("fateFlowModelLoader");
            case "FILE":
                return (ModelLoader) applicationContext.getBean("localFileModelLoader");
            case "PB":
                return (ModelLoader) applicationContext.getBean("localPbModelLoader");
            default:
                return null;

        }

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
