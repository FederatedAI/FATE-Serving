package com.webank.ai.fate.serving.core.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AsyncSubscribeRegister implements ApplicationContextAware, ApplicationListener<ApplicationReadyEvent> {
    Logger logger = LoggerFactory.getLogger(AsyncSubscribeRegister.class);

    ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.applicationContext = context;
    }

    public static final Map<String, List<Method>> SUBSCRIBE_METHOD_MAP = new HashMap<>();

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationEvent) {
        AsyncMessageProcessor bean = applicationContext.getBean(AsyncMessageProcessor.class);

        Method[] methods = bean.getClass().getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Subscribe.class)) {
                Subscribe subscribe = method.getAnnotation(Subscribe.class);
                if (subscribe != null) {
                    List<Method> methodList = SUBSCRIBE_METHOD_MAP.get(subscribe.name());
                    if (methodList == null) {
                        methodList = new ArrayList<>();
                    }
                    methodList.add(method);
                    SUBSCRIBE_METHOD_MAP.put(subscribe.name(), methodList);
                }
            }
        }

        logger.info("subscribe register info {}", SUBSCRIBE_METHOD_MAP);
    }
}
