package com.webank.ai.fate.serving;

import com.webank.ai.fate.serving.core.async.AsyncSubscribeRegister;
import com.webank.ai.fate.serving.core.async.DefaultAsyncMessageProcessor;
import com.webank.ai.fate.serving.core.bean.SpringContextUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AsyncMessageConfig {

    @Bean
    @ConditionalOnMissingBean
    public SpringContextUtil springContextUtil() {
        return new SpringContextUtil();
    }

    @Bean
    public AsyncSubscribeRegister asyncSubscribeRegister() {
        return new AsyncSubscribeRegister();
    }

    @Bean
    public DefaultAsyncMessageProcessor defaultAsyncMessageProcessor() {
        return new DefaultAsyncMessageProcessor();
    }

}
