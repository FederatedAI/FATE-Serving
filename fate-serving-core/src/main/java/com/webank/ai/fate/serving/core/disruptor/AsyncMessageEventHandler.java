package com.webank.ai.fate.serving.core.disruptor;

import com.lmax.disruptor.EventHandler;
import com.webank.ai.fate.serving.core.async.AsyncMessageProcessor;
import com.webank.ai.fate.serving.core.async.AsyncSubscribeRegister;
import com.webank.ai.fate.serving.core.exceptions.AsyncMessageException;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;

/**
 * Consumer
 */
public class AsyncMessageEventHandler implements EventHandler<AsyncMessageEvent> {

    @Override
    public void onEvent(AsyncMessageEvent event, long sequence, boolean endOfBatch) throws Exception {
        System.out.println("Thread.currentThread().getName() = " + Thread.currentThread().getName());
        String eventName = event.getName();

        if (StringUtils.isBlank(eventName)) {
            throw new AsyncMessageException("eventName is blank");
        }

        Method method = AsyncSubscribeRegister.SUBSCRIBE_METHOD_MAP.get(eventName);
        if (method == null) {
            throw new AsyncMessageException(eventName + " event not subscribe");
        }

        // invoke event processor
        method.invoke(AsyncMessageProcessor.getInstance(), event);
    }

}
