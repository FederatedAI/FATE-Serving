package com.webank.ai.fate.serving.proxy.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Description TODO
 * @Author
 **/
@Configuration
public class GrpcConfigration {


    private static final Logger logger = LoggerFactory.getLogger(GrpcConfigration.class);

    @Value("${proxy.grpc.threadpool.coresize:50}")
    private int  coreSize;

    @Value("${proxy.grpc.threadpool.maxsize:100}")
    private int  maxPoolSize;

    @Value("${proxy.grpc.threadpool.queuesize:10}")
    private int  queueSize;

    @Bean(name="grpcExecutorPool")
    public Executor asyncServiceExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(coreSize);

        executor.setMaxPoolSize(maxPoolSize);

        executor.setQueueCapacity(queueSize);

        executor.setThreadNamePrefix("grpc-service");

        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());

        executor.initialize();

        return executor;
    }


}
