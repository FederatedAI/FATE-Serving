package com.webank.ai.fate.serving.event;

import com.webank.ai.fate.register.utils.StringUtils;
import com.webank.ai.fate.serving.common.async.AbstractAsyncMessageProcessor;
import com.webank.ai.fate.serving.common.async.AsyncMessageEvent;
import com.webank.ai.fate.serving.common.async.Subscribe;
import com.webank.ai.fate.serving.common.bean.ServingServerContext;
import com.webank.ai.fate.serving.common.flow.FlowCounterManager;
import com.webank.ai.fate.serving.common.model.Model;

import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;


@Service
public class ErrorEventHandler extends AbstractAsyncMessageProcessor implements EnvironmentAware, InitializingBean {

    @Autowired
    FlowCounterManager flowCounterManager;

    Environment environment;

    @Subscribe(value = Dict.EVENT_ERROR)
    public void handleMetricsEvent(AsyncMessageEvent event) {
        Context context = event.getContext();
        String serviceName = context.getServiceName();
        String actionType = context.getActionType();
        String resource = StringUtils.isNotEmpty(actionType) ? actionType : serviceName;
        flowCounterManager.exception(resource);
        if (context instanceof ServingServerContext) {
            ServingServerContext servingServerContext = (ServingServerContext) context;
            Model model = servingServerContext.getModel();
            if (model != null) {
                flowCounterManager.exception(model.getResourceName());
            }
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
//        String clazz = environment.getProperty(ALTER_CLASS, "com.webank.ai.fate.serving.core.upload.MockAlertInfoUploader");
//        alertInfoUploader = (AlertInfoUploader) InferenceUtils.getClassByName(clazz);
    }
}
