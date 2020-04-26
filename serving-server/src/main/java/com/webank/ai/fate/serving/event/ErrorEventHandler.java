package com.webank.ai.fate.serving.event;

import com.webank.ai.fate.serving.core.annotation.Subscribe;
import com.webank.ai.fate.serving.core.async.AsyncMessageEvent;
import com.webank.ai.fate.serving.core.upload.AlertInfoUploader;
import com.webank.ai.fate.serving.core.utils.InferenceUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * @Description TODO
 * @Author kaideng
 **/
@Service

public class ErrorEventHandler implements EnvironmentAware ,InitializingBean {
    Environment   environment;
    static  final  String  ALTER_CLASS = "alertClass";
    AlertInfoUploader  alertInfoUploader = null;
    @Subscribe("error")
    public void handleMetricsEvent(AsyncMessageEvent  event){
        alertInfoUploader.upload(event);
    }
    @Override
    public void setEnvironment(Environment environment) {
        this.environment =  environment;
    }
    @Override
    public void afterPropertiesSet() throws Exception {
        String  clazz = environment.getProperty(ALTER_CLASS,"com.webank.ai.fate.serving.core.upload.MockAlertInfoUploader");
        alertInfoUploader =(AlertInfoUploader)InferenceUtils.getClassByName(clazz);
    }
}
