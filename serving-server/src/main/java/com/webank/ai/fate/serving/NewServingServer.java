package com.webank.ai.fate.serving;

import com.webank.ai.fate.register.common.NamedThreadFactory;
import com.webank.ai.fate.register.provider.FateServerBuilder;
import com.webank.ai.fate.serving.core.bean.Configuration;
import com.webank.ai.fate.serving.service.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Description TODO
 * @Author kaideng
 **/
@Service
public class NewServingServer implements InitializingBean{

    @Value("")
    int port;

    private Server server;
    @Autowired
    InferenceService  inferenceService;
    @Autowired
    ModelService  modelService;
    @Autowired
    ProxyService  proxyService;




    @Override
    public void afterPropertiesSet() throws Exception {

        int processors = Runtime.getRuntime().availableProcessors();
        Integer corePoolSize = Configuration.getPropertyInt("serving.core.pool.size", processors);
        Integer maxPoolSize = Configuration.getPropertyInt("serving.max.pool.size", processors * 2);
        Integer aliveTime = Configuration.getPropertyInt("serving.pool.alive.time", 1000);
        Integer queueSize = Configuration.getPropertyInt("serving.pool.queue.size", 10);
        Executor executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, aliveTime.longValue(), TimeUnit.MILLISECONDS,
                new SynchronousQueue(), new NamedThreadFactory("ServingServer", true));

        FateServerBuilder serverBuilder = (FateServerBuilder) ServerBuilder.forPort(port);
        serverBuilder.keepAliveTime(100,TimeUnit.MILLISECONDS);
        serverBuilder.executor(executor);
        //new ServiceOverloadProtectionHandle()
        serverBuilder.addService(ServerInterceptors.intercept(inferenceService, new ServiceExceptionHandler(), new ServiceOverloadProtectionHandle()), InferenceService.class);
        serverBuilder.addService(ServerInterceptors.intercept(modelService, new ServiceExceptionHandler(), new ServiceOverloadProtectionHandle()), ModelService.class);
        serverBuilder.addService(ServerInterceptors.intercept(proxyService, new ServiceExceptionHandler(), new ServiceOverloadProtectionHandle()), ProxyService.class);
        server = serverBuilder.build();

    }
}
