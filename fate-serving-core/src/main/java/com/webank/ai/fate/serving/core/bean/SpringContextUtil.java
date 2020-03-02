package com.webank.ai.fate.serving.core.bean;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringContextUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext = null;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public static <T> T getBean(String beanName) {
        return (T) applicationContext.getBean(beanName);
    }

    public static <T> T getBean(String beanName, Class requiredType) {
        return (T) applicationContext.getBean(beanName, requiredType);
    }

    public static <T> T getBean(Class requiredType) {
        return (T) applicationContext.getBean(requiredType);
    }

    public static String[] getBeanNamesForType(Class type) {
        return applicationContext.getBeanNamesForType(type);
    }

}
