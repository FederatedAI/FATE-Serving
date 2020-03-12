package com.webank.ai.fate.serving.core.async;

import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.lmax.disruptor.EventHandler;
import com.webank.ai.fate.serving.core.exceptions.AsyncMessageException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Consumer
 */
public class AsyncMessageEventHandler implements EventHandler<AsyncMessageEvent> {

    private static Logger logger = LoggerFactory.getLogger(AsyncMessageEventHandler.class);

    ExecutorService executorService = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(), new NamedThreadFactory("AsyncMessage", true));

    @Override
    public void onEvent(AsyncMessageEvent event, long sequence, boolean endOfBatch) throws Exception {
        String eventName = event.getName();

        logger.info("Async event: {}, {}", eventName, event);

        if (StringUtils.isBlank(eventName)) {
            throw new AsyncMessageException("eventName is blank");
        }

        Set<Method> methods = AsyncSubscribeRegister.SUBSCRIBE_METHOD_MAP.get(eventName);
        if (methods == null || methods.size() == 0) {
            throw new AsyncMessageException(eventName + " event not subscribe");
        }

        for (Method method : methods) {
            executorService.submit(() -> {
                // invoke event processor
                try {
                    Class<?> declaringClass = method.getDeclaringClass();
                    method.invoke(declaringClass.newInstance(), event);
                } catch (Exception e) {
                    logger.error("invoke event processor, {}", e.getMessage());
                    e.printStackTrace();
                }
            });
        }
    }

}
