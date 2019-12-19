package com.webank.ai.fate.serving.proxy.config;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.request.async.TimeoutCallableProcessingInterceptor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.ServletContext;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration

public class WebConfigration implements WebMvcConfigurer {


    private final Logger logger = LoggerFactory.getLogger(WebConfigration.class);

    @Autowired
    ServletContext servletContext;

    @Value("${proxy.async.timeout:5000}")
    long  timeout;

    @Value("${proxy.async.coresize:10}")
    int  coreSize;

    @Value("${proxy.async.maxsize:100}")
    int  maxSize;

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(coreSize);
        executor.setMaxPoolSize(maxSize);
        executor.setThreadNamePrefix("GatewayAsync");

        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.initialize();
        configurer.setTaskExecutor(executor);
        configurer.setDefaultTimeout(timeout);
        configurer.registerCallableInterceptors(new TimeoutCallableProcessingInterceptor());
    }

    @Bean
    public TimeoutCallableProcessingInterceptor timeoutCallableProcessingInterceptor() {
        return new TimeoutCallableProcessingInterceptor();
    }


    @Bean

    public FilterRegistrationBean initUserServletFilterRegistration() {



        FilterRegistrationBean registration = new FilterRegistrationBean();
        CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
        registration.setFilter(characterEncodingFilter);

        registration.addUrlPatterns("/*");
        registration.setName("CharacterEncodingFilter");
        registration.setOrder(Integer.MAX_VALUE);

        return registration;
    }

}
