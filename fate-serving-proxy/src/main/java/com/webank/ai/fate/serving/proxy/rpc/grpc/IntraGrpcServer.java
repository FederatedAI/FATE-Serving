package com.webank.ai.fate.serving.proxy.rpc.grpc;

import com.webank.ai.fate.register.provider.FateServer;
import com.webank.ai.fate.register.provider.FateServerBuilder;
import com.webank.ai.fate.register.zookeeper.ZookeeperRegistry;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.Executor;

/**
 * @Description TODO
 * @Author
 **/
@Service
public class IntraGrpcServer implements InitializingBean {
    Logger logger = LoggerFactory.getLogger(InterGrpcServer.class);
    @Autowired
    IntraRequestHandler intraRequestHandler;
    @Autowired
    CommonRequestHandler commonRequestHandler;
    @Autowired
    ZookeeperRegistry zookeeperRegistry;
    @Resource(name = "grpcExecutorPool")
    Executor executor;
    Server server;
    @Value("${proxy.grpc.intra.port:8867}")
    private Integer port;

    @Override
    public void afterPropertiesSet() throws Exception {
        FateServerBuilder serverBuilder = (FateServerBuilder) ServerBuilder.forPort(port);
        serverBuilder.executor(executor);
        serverBuilder.addService(ServerInterceptors.intercept(intraRequestHandler, new ServiceExceptionHandler()));
        serverBuilder.addService(ServerInterceptors.intercept(commonRequestHandler, new ServiceExceptionHandler()), CommonRequestHandler.class);
        server = serverBuilder.build();
        server.start();

        logger.info("register zk , {}", FateServer.serviceSets);
        zookeeperRegistry.register(FateServer.serviceSets);
    }
}

