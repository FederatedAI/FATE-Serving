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

import com.webank.ai.fate.serving.core.bean.MetaInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class GrpcConfigration {

    @Bean(name = "grpcExecutorPool")
    public Executor asyncServiceExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(MetaInfo.PROPERTY_PROXY_GRPC_THREADPOOL_CORESIZE);

        executor.setMaxPoolSize(MetaInfo.PROPERTY_PROXY_GRPC_THREADPOOL_MAXSIZE);

        executor.setQueueCapacity(MetaInfo.PROPERTY_PROXY_GRPC_THREADPOOL_QUEUESIZE);

        executor.setThreadNamePrefix("grpc-service");

        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());

        executor.initialize();

        return executor;
    }

}
