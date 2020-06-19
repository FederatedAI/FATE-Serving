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
    private int coreSize;

    @Value("${proxy.grpc.threadpool.maxsize:100}")
    private int maxPoolSize;

    @Value("${proxy.grpc.threadpool.queuesize:10}")
    private int queueSize;

    @Bean(name = "grpcExecutorPool")
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
