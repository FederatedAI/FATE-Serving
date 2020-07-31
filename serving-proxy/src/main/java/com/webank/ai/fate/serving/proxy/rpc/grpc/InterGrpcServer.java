package com.webank.ai.fate.serving.proxy.rpc.grpc;
import com.webank.ai.fate.register.provider.FateServerBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.*;
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

    @Value("${proxy.grpc.inter.port:8869}")
    private Integer port;

    @Value("${proxy.grpc.inter.negotiationType:PLAINTEXT}")
    private String negotiationType;

    @Value("${proxy.grpc.inter.client.certChain.file:}")
    private String certChainFilePath;

    @Value("${proxy.grpc.inter.client.privateKey.file:}")
    private String privateKeyFilePath;

    @Value("${proxy.grpc.inter.CA.file:}")
    private String trustCertCollectionFilePath;

    Logger logger  = LoggerFactory.getLogger(InterGrpcServer.class);

    Server server ;

    public Server getServer() {
        return server;
    }

    @Autowired
    InterRequestHandler interRequestHandler;

    @Resource(name="grpcExecutorPool")
    Executor executor;

    @Override
    public void afterPropertiesSet() throws Exception {
        FateServerBuilder serverBuilder = null;
        if("TLS".equals(negotiationType)) {
            if(certChainFilePath.isEmpty() || privateKeyFilePath.isEmpty() || trustCertCollectionFilePath.isEmpty()) {
                throw new RuntimeException("using TLS, but certificates file paths are missing!");
            }
            try {
                SslContextBuilder sslContextBuilder = GrpcSslContexts.forServer(new File(certChainFilePath), new File(privateKeyFilePath))
                        .trustManager(new File(trustCertCollectionFilePath))
                        .clientAuth(ClientAuth.REQUIRE)
                        .sessionTimeout(3600 << 4)
                        .sessionCacheSize(65536);

                serverBuilder = new FateServerBuilder(NettyServerBuilder.forPort(port));
                serverBuilder.sslContext(sslContextBuilder.build());
            } catch (SSLException e) {
                throw new SecurityException(e);
            }
            logger.info("running in secure mode. server crt path: {}, server key path: {}, ca crt path: {}.",
                    certChainFilePath, privateKeyFilePath, trustCertCollectionFilePath);
        } else {
            serverBuilder = (FateServerBuilder) ServerBuilder.forPort(port);
            logger.info("running in insecure mode.");
        }

        serverBuilder.executor(executor);
        serverBuilder.addService(ServerInterceptors.intercept(interRequestHandler, new ServiceExceptionHandler()));
        serverBuilder.addService(interRequestHandler);
        server = serverBuilder.build();
        server.start();

    }
}
