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

            SslContextBuilder sslContextBuilder = SslContextBuilder.forServer(new File(certChainFilePath), new File(privateKeyFilePath));
            sslContextBuilder.trustManager(new File(trustCertCollectionFilePath));
            sslContextBuilder.clientAuth(ClientAuth.REQUIRE);

            serverBuilder = new FateServerBuilder(NettyServerBuilder.forPort(port));
            serverBuilder.sslContext(GrpcSslContexts.configure(sslContextBuilder, SslProvider.OPENSSL).build());
        } else {
            serverBuilder = (FateServerBuilder) ServerBuilder.forPort(port);
        }

        serverBuilder.executor(executor);
        serverBuilder.addService(ServerInterceptors.intercept(interRequestHandler, new ServiceExceptionHandler()));
        serverBuilder.addService(interRequestHandler);
        server = serverBuilder.build();
        server.start();

    }
}
