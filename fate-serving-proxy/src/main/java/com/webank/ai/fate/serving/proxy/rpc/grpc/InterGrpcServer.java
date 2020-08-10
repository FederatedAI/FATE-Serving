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

package com.webank.ai.fate.serving.proxy.rpc.grpc;

import com.webank.ai.fate.register.provider.FateServerBuilder;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.Executor;

/**
 * @Description TODO
 * @Author
 **/
@Service
public class InterGrpcServer implements InitializingBean {

    Logger logger = LoggerFactory.getLogger(InterGrpcServer.class);
    Server server;
    @Autowired
    InterRequestHandler interRequestHandler;
    @Resource(name = "grpcExecutorPool")
    Executor executor;

    @Override
    public void afterPropertiesSet() throws Exception {
        FateServerBuilder serverBuilder = (FateServerBuilder) ServerBuilder.forPort(MetaInfo.PROPERTY_PROXY_GRPC_INTER_PORT);
        serverBuilder.executor(executor);
        serverBuilder.addService(ServerInterceptors.intercept(interRequestHandler, new ServiceExceptionHandler()), InterRequestHandler.class);
        server = serverBuilder.build();
        server.start();
    }
}
