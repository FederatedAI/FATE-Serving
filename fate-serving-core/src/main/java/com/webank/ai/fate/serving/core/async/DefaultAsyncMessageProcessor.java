package com.webank.ai.fate.serving.core.async;

import com.webank.ai.fate.serving.core.bean.Dict;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultAsyncMessageProcessor extends AbstractAsyncMessageProcessor {

    private static Logger logger = LoggerFactory.getLogger(DefaultAsyncMessageProcessor.class);

    @Subscribe(Dict.EVENT_INFERENCE)
    public void processInferenceEvent(AsyncMessageEvent event) {
//        logger.info("Process inference event..");
    }

    @Subscribe(Dict.EVENT_UNARYCALL)
    public void processUnaryCallEvent(AsyncMessageEvent event) {
//        logger.info("Process unaryCall event..");
    }

    @Subscribe(Dict.EVENT_ERROR)
    public void processErrorEvent(AsyncMessageEvent event) {
//        logger.info("Process error event..");
    }

}
