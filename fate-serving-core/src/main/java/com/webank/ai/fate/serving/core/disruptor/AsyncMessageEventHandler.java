package com.webank.ai.fate.serving.core.disruptor;

import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.lmax.disruptor.EventHandler;
import com.webank.ai.fate.serving.core.async.AsyncSubscribeRegister;
import com.webank.ai.fate.serving.core.exceptions.AsyncMessageException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Consumer
 */
@Slf4j
public class AsyncMessageEventHandler implements EventHandler<AsyncMessageEvent> {

    ExecutorService executorService = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(), new NamedThreadFactory("AsyncMessage", true));

    @Override
    public void onEvent(AsyncMessageEvent event, long sequence, boolean endOfBatch) throws Exception {
        String eventName = event.getName();

        log.info("Async event: {}, {}", eventName, event);

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
                    log.error("invoke event processor, {}", e.getMessage());
                    e.printStackTrace();
                }
            });
        }
    }

}
