package com.webank.ai.fate.serving.proxy.config;

import com.webank.ai.fate.serving.core.async.Subscribe;
import com.webank.ai.fate.serving.core.async.AbstractAsyncMessageProcessor;
import com.webank.ai.fate.serving.core.async.AsyncSubscribeRegister;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.SpringContextUtil;
import com.webank.ai.fate.serving.core.flow.FlowCounterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class ProxyConfig {

    Logger logger = LoggerFactory.getLogger(ProxyConfig.class);

    @Bean
    @ConditionalOnMissingBean
    public SpringContextUtil springContextUtil() {
        return new SpringContextUtil();
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> asyncSubscribeRegister() {

        return new ApplicationListener<ApplicationReadyEvent>() {
            @Override
            public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
                String[] beans = SpringContextUtil.getBeanNamesForType(AbstractAsyncMessageProcessor.class);
                for (String beanName : beans) {
                    AbstractAsyncMessageProcessor eventProcessor = SpringContextUtil.getBean(beanName, AbstractAsyncMessageProcessor.class);
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
            }
        };
    }

    @Bean
    public FlowCounterManager flowCounterManager() {
        FlowCounterManager flowCounterManager = new FlowCounterManager(Dict.SERVICE_PROXY);
        flowCounterManager.startReport();
        return flowCounterManager;
    }

}
