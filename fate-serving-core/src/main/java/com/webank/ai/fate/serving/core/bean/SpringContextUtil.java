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

    public static String getProperty(String key) {
        return applicationContext.getEnvironment().getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return applicationContext.getEnvironment().getProperty(key, defaultValue);
    }

    public static <T> T getProperty(String key, Class<T> targetType) {
        return applicationContext.getEnvironment().getProperty(key, targetType);
    }

    public static <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        return applicationContext.getEnvironment().getProperty(key, targetType, defaultValue);
    }

}
