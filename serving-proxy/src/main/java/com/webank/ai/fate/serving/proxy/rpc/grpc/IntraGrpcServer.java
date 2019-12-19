package com.webank.ai.fate.serving.proxy.rpc.grpc;
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

    @Value("${proxy.grpc.intra.port:8867}")
    private Integer port;

    Logger logger  = LoggerFactory.getLogger(InterGrpcServer.class);

    Server server ;

    @Autowired
    IntraRequestHandler intraRequestHandler;

    @Resource(name="grpcExecutorPool")
    Executor executor;

    @Override
    public void afterPropertiesSet() throws Exception {
        ServerBuilder serverBuilder = ServerBuilder.forPort(port);
        serverBuilder.executor(executor);
        serverBuilder.addService(ServerInterceptors.intercept(intraRequestHandler, new ServiceExceptionHandler()));
        serverBuilder.addService(intraRequestHandler);
        server = serverBuilder.build();
        server.start();

    }
}

