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

package com.webank.ai.fate.serving;

import com.webank.ai.fate.serving.common.async.AbstractAsyncMessageProcessor;
import com.webank.ai.fate.serving.common.async.AsyncSubscribeRegister;
import com.webank.ai.fate.serving.common.async.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class AsyncMessageConfig implements ApplicationContextAware {

    Logger logger = LoggerFactory.getLogger(AsyncMessageConfig.class);

    ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> asyncSubscribeRegister() {
        return applicationReadyEvent -> {
            String[] beans = applicationContext.getBeanNamesForType(AbstractAsyncMessageProcessor.class);
            for (String beanName : beans) {
                AbstractAsyncMessageProcessor eventProcessor = applicationContext.getBean(beanName, AbstractAsyncMessageProcessor.class);
                Method[] methods = eventProcessor.getClass().getMethods();
                for (Method method : methods) {
                    if (method.isAnnotationPresent(Subscribe.class)) {
                        Subscribe subscribe = method.getAnnotation(Subscribe.class);
                        if (subscribe != null) {
                            Set<Method> methodList = AsyncSubscribeRegister.SUBSCRIBE_METHOD_MAP.get(subscribe.value());
                            if (methodList == null) {
                                methodList = new HashSet<>();
                            }
                            methodList.add(method);
                            AsyncSubscribeRegister.METHOD_INSTANCE_MAP.put(method, eventProcessor);
                            AsyncSubscribeRegister.SUBSCRIBE_METHOD_MAP.put(subscribe.value(), methodList);
                        }
                    }
                }
            }

            logger.info("subscribe register info {}", AsyncSubscribeRegister.SUBSCRIBE_METHOD_MAP.keySet());
        };
    }

}
