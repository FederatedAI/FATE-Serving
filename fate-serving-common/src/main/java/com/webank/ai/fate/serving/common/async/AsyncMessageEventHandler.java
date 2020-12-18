/*
 * Copyright 2019 The FATE Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.ai.fate.serving.common.async;

import com.lmax.disruptor.EventHandler;
import com.webank.ai.fate.serving.core.exceptions.AsyncMessageException;
import com.webank.ai.fate.serving.core.utils.ThreadPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * Consumer
 */
public class AsyncMessageEventHandler implements EventHandler<AsyncMessageEvent> {

    private static Logger logger = LoggerFactory.getLogger(AsyncMessageEventHandler.class);

    ExecutorService executorService = null;

    public AsyncMessageEventHandler() {

        executorService = ThreadPoolUtil.newThreadPoolExecutor();

    }

    @Override
    public void onEvent(AsyncMessageEvent event, long sequence, boolean endOfBatch) throws Exception {
        String eventName = event.getName();

        logger.info("Async event: {}", eventName);

        if (StringUtils.isBlank(eventName)) {
            throw new AsyncMessageException("eventName is blank");
        }

        Set<Method> methods = AsyncSubscribeRegister.SUBSCRIBE_METHOD_MAP.get(eventName);
        if (methods == null || methods.size() == 0) {
            logger.error("event {} not subscribe {}", eventName, AsyncSubscribeRegister.SUBSCRIBE_METHOD_MAP);
            throw new AsyncMessageException(eventName + " event not subscribe");

        }

        AsyncMessageEvent another = event.clone();

        for (Method method : methods) {
            executorService.submit(() -> {
                try {
                    Object object = AsyncSubscribeRegister.METHOD_INSTANCE_MAP.get(method);
                    method.invoke(object, another);
                } catch (Exception e) {
                    logger.error("invoke event processor, {}", e.getMessage());
                    e.printStackTrace();
                }
            });
        }
    }

}
