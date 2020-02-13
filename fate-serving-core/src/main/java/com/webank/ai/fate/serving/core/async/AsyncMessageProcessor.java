package com.webank.ai.fate.serving.core.async;

import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.disruptor.AsyncMessageEvent;

public class AsyncMessageProcessor {

    private static AsyncMessageProcessor instance = null;

    public static AsyncMessageProcessor getInstance() {
        if (instance == null) {
            instance = new AsyncMessageProcessor();
        }
        return instance;
    }

    @Subscribe(name = Dict.EVENT_INFERENCE)
    public void processInferenceEvent(AsyncMessageEvent event) {
        // TODO: 2020/2/13  
    }

    @Subscribe(name = Dict.EVENT_UNARYCALL)
    public void processUnarycallEvent(AsyncMessageEvent event) {
        // TODO: 2020/2/13
    }

}
