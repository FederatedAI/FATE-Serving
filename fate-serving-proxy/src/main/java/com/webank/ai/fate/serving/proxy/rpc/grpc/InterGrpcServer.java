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
import com.webank.ai.fate.register.utils.StringUtils;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NegotiationType;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.ClientAuth;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.net.ssl.SSLException;
import java.io.File;
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
        int port = MetaInfo.PROPERTY_PROXY_GRPC_INTER_PORT;
        String negotiationType = MetaInfo.PROPERTY_PROXY_GRPC_INTER_NEGOTIATIONTYPE;
        String certChainFilePath = MetaInfo.PROPERTY_PROXY_GRPC_INTER_SERVER_CERTCHAIN_FILE;
        String privateKeyFilePath = MetaInfo.PROPERTY_PROXY_GRPC_INTER_SERVER_PRIVATEKEY_FILE;
        String trustCertCollectionFilePath = MetaInfo.PROPERTY_PROXY_GRPC_INTER_CA_FILE;

        FateServerBuilder serverBuilder;
        if(NegotiationType.TLS == NegotiationType.valueOf(negotiationType)) {
            if(StringUtils.isBlank(certChainFilePath) || StringUtils.isBlank(privateKeyFilePath) || StringUtils.isBlank(trustCertCollectionFilePath)) {
                throw new RuntimeException("using TLS, but certificates file paths are missing!");
            }
            try {
                SslContextBuilder sslContextBuilder = GrpcSslContexts.forServer(new File(certChainFilePath), new File(privateKeyFilePath))
                        .trustManager(new File(trustCertCollectionFilePath))
                        .clientAuth(ClientAuth.REQUIRE)
                        .sessionTimeout(3600 << 4)
                        .sessionCacheSize(65536);

                GrpcSslContexts.configure(sslContextBuilder, SslProvider.OPENSSL);

                serverBuilder = new FateServerBuilder(NettyServerBuilder.forPort(port));
                serverBuilder.sslContext(sslContextBuilder.build());

                logger.info("running in secure mode. server crt path: {}, server key path: {}, ca crt path: {}.",
                        certChainFilePath, privateKeyFilePath, trustCertCollectionFilePath);
            } catch (SSLException e) {
                throw new SecurityException(e);
            }
        } else {
            serverBuilder = (FateServerBuilder) ServerBuilder.forPort(port);
            logger.info("running in insecure mode.");
        }

        serverBuilder.executor(executor);
        serverBuilder.addService(ServerInterceptors.intercept(interRequestHandler, new ServiceExceptionHandler()), InterRequestHandler.class);
        server = serverBuilder.build();
        server.start();
    }
}
