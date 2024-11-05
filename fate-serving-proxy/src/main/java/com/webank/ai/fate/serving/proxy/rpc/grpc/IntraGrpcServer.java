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

import com.webank.ai.fate.register.provider.FateServer;
import com.webank.ai.fate.register.provider.FateServerBuilder;
import com.webank.ai.fate.register.zookeeper.ZookeeperRegistry;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Service
public class IntraGrpcServer implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(InterGrpcServer.class);

    @Autowired
    IntraRequestHandler intraRequestHandler;

    @Autowired
    CommonRequestHandler commonRequestHandler;

    @Autowired
    RouterTableService routerTableService;

    @Autowired(required = false)
    ZookeeperRegistry zookeeperRegistry;

    @Resource(name = "grpcExecutorPool")
    Executor executor;

    private Server server;

    @Override
    public void afterPropertiesSet() throws Exception {
        FateServerBuilder serverBuilder = (FateServerBuilder) ServerBuilder.forPort(MetaInfo.PROPERTY_PROXY_GRPC_INTRA_PORT);
        serverBuilder.executor(executor);
        serverBuilder.addService(ServerInterceptors.intercept(intraRequestHandler, new ServiceExceptionHandler()), IntraRequestHandler.class);
        serverBuilder.addService(ServerInterceptors.intercept(commonRequestHandler, new ServiceExceptionHandler()), CommonRequestHandler.class);
        serverBuilder.addService(ServerInterceptors.intercept(routerTableService, new ServiceExceptionHandler()), RouterTableService.class);
        server = serverBuilder.build();
        server.start();

        if (zookeeperRegistry != null) {
            logger.info("register zk , {}", FateServer.serviceSets);
            zookeeperRegistry.register(FateServer.serviceSets);
        }
    }

    @PreDestroy
    public void destroy() throws InterruptedException {
        logger.info("start to shutdown IntraGrpcServer and await termination......");
        try {
            if (server != null) {
                server.shutdown();
                server.awaitTermination(10, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            logger.error("shutdown IntraGrpcServer happen exception {}", e.getMessage());
            throw e;
        }
        logger.info("IntraGrpcServer is shutdown");
    }
}

