package com.webank.ai.fate.serving.core.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Component
public class AsyncSubscribeRegister implements ApplicationContextAware, ApplicationListener<ApplicationEvent> {
    Logger logger = LoggerFactory.getLogger(AsyncSubscribeRegister.class);

    ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.applicationContext = context;
    }

    public static final Map<String, Method> SUBSCRIBE_METHOD_MAP = new HashMap<>();

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof ContextRefreshedEvent) {
            AsyncMessageProcessor bean = applicationContext.getBean(AsyncMessageProcessor.class);

            Method[] methods = bean.getClass().getMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(Subscribe.class)) {
                    Subscribe subscribe = method.getAnnotation(Subscribe.class);
                    if (subscribe != null) {
                        SUBSCRIBE_METHOD_MAP.put(subscribe.name(), method);
                    }
                }
            }

            logger.info("subscribe register info {}", SUBSCRIBE_METHOD_MAP);
        }
    }
}
