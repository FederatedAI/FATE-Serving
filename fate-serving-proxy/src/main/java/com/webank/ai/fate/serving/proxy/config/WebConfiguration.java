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

package com.webank.ai.fate.serving.proxy.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.request.async.TimeoutCallableProcessingInterceptor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    private final Logger logger = LoggerFactory.getLogger(WebConfiguration.class);
    int processors = Runtime.getRuntime().availableProcessors();

    @Value("${proxy.async.timeout:5000}")
    long timeout;

    @Value("${proxy.async.coresize:0}")
    int coreSize;

    @Value("${proxy.async.maxsize:0}")
    int maxSize;

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(coreSize > 0 ? coreSize : processors);
        executor.setMaxPoolSize(maxSize > 0 ? maxSize : 2 * processors);
        executor.setThreadNamePrefix("ProxyAsync");
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
