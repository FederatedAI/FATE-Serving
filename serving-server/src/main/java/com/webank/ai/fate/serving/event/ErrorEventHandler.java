package com.webank.ai.fate.serving.event;

import com.webank.ai.fate.serving.core.async.Subscribe;
import com.webank.ai.fate.serving.core.async.AbstractAsyncMessageProcessor;
import com.webank.ai.fate.serving.core.async.AsyncMessageEvent;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.upload.AlertInfoUploader;
import com.webank.ai.fate.serving.core.utils.InferenceUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;


@Service
public class ErrorEventHandler extends AbstractAsyncMessageProcessor implements EnvironmentAware, InitializingBean {
    static final String ALTER_CLASS = "alertClass";
    Environment environment;
    AlertInfoUploader alertInfoUploader = null;

    @Subscribe(value = Dict.EVENT_ERROR)
    public void handleMetricsEvent(AsyncMessageEvent event) {
        alertInfoUploader.upload(event);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String clazz = environment.getProperty(ALTER_CLASS, "com.webank.ai.fate.serving.core.upload.MockAlertInfoUploader");
        alertInfoUploader = (AlertInfoUploader) InferenceUtils.getClassByName(clazz);
    }
}
