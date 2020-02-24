package com.webank.ai.fate.serving.core.disruptor;

import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.lmax.disruptor.EventHandler;
import com.webank.ai.fate.serving.core.async.AsyncMessageProcessor;
import com.webank.ai.fate.serving.core.async.AsyncSubscribeRegister;
import com.webank.ai.fate.serving.core.exceptions.AsyncMessageException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Consumer
 */
public class AsyncMessageEventHandler implements EventHandler<AsyncMessageEvent> {
    Logger logger =  LoggerFactory.getLogger(AsyncMessageProcessor.class);

    ExecutorService executorService = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(), new NamedThreadFactory("AsyncMessage", true));

    @Override
    public void onEvent(AsyncMessageEvent event, long sequence, boolean endOfBatch) throws Exception {
        String eventName = event.getName();

        if (StringUtils.isBlank(eventName)) {
            throw new AsyncMessageException("eventName is blank");
        }

        List<Method> methods = AsyncSubscribeRegister.SUBSCRIBE_METHOD_MAP.get(eventName);
        if (methods == null || methods.size() == 0) {
            throw new AsyncMessageException(eventName + " event not subscribe");
        }

        for (Method method : methods) {
            executorService.submit(() -> {
                // invoke event processor
                try {
                    method.invoke(AsyncMessageProcessor.getInstance(), event);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            });
        }
    }

}
