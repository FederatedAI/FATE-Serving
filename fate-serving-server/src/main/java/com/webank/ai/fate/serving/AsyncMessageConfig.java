package com.webank.ai.fate.serving;

import com.webank.ai.fate.serving.core.async.AbstractAsyncMessageProcessor;
import com.webank.ai.fate.serving.core.async.AsyncSubscribeRegister;
import com.webank.ai.fate.serving.core.async.Subscribe;
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
