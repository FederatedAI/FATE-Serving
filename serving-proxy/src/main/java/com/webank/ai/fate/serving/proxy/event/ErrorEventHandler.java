package com.webank.ai.fate.serving.proxy.event;

import com.webank.ai.fate.serving.core.annotation.Subscribe;
import com.webank.ai.fate.serving.core.async.AbstractAsyncMessageProcessor;
import com.webank.ai.fate.serving.core.async.AsyncMessageEvent;
import com.webank.ai.fate.serving.core.bean.Dict;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;


@Service
public class ErrorEventHandler extends AbstractAsyncMessageProcessor implements InitializingBean {

    @Autowired
    Environment environment;

    @Subscribe(value = Dict.EVENT_ERROR)
    public void handleMetricsEvent(AsyncMessageEvent event) {
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }
}
