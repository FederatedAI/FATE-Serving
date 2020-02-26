package com.webank.ai.fate.serving.proxy.config;

import com.webank.ai.fate.serving.core.async.AsyncMessageProcessor;
import com.webank.ai.fate.serving.core.async.AsyncSubscribeRegister;
import com.webank.ai.fate.serving.core.bean.SpringContextUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AsyncMessageConfig {

    @Bean
    public SpringContextUtil springContextUtil() {
        return new SpringContextUtil();
    }

    @Bean
    public AsyncSubscribeRegister asyncSubscribeRegister() {
        return new AsyncSubscribeRegister();
    }

    @Bean
    public AsyncMessageProcessor asyncSubscribeProcesser() {
        return new AsyncMessageProcessor();
    }

}
