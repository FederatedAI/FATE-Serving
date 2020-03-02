package com.webank.ai.fate.serving.core.async;

import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.disruptor.AsyncMessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class DefaultAsyncMessageProcessor extends AbstractAsyncMessageProcessor {

    @Subscribe(Dict.EVENT_INFERENCE)
    public void processInferenceEvent(AsyncMessageEvent event) {
        log.info("Process inference event..");
    }

    @Subscribe(Dict.EVENT_UNARYCALL)
    public void processUnaryCallEvent(AsyncMessageEvent event) {
        log.info("Process unaryCall event..");
    }

}
