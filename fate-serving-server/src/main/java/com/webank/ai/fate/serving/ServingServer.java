package com.webank.ai.fate.serving;

import com.webank.ai.fate.register.common.NamedThreadFactory;
import com.webank.ai.fate.register.provider.FateServer;
import com.webank.ai.fate.register.provider.FateServerBuilder;
import com.webank.ai.fate.register.zookeeper.ZookeeperRegistry;
import com.webank.ai.fate.serving.core.bean.BaseContext;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import com.webank.ai.fate.serving.core.utils.HttpClientPool;
import com.webank.ai.fate.serving.grpc.service.*;
import com.webank.ai.fate.serving.model.ModelManager;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

@Service
public class ServingServer implements InitializingBean {

    Logger logger = LoggerFactory.getLogger(ServingServer.class);

    @Autowired
    GuestInferenceService guestInferenceService;
    @Autowired
    CommonService commonService;
    @Autowired
    ModelManager modelManager;
    @Autowired
    ModelService modelService;
    @Autowired
    HostInferenceService hostInferenceService;
    @Autowired(required = false)
    ZookeeperRegistry zookeeperRegistry;
    @Autowired
    Environment environment;
    private Server server;

    @Override
    public void afterPropertiesSet() throws Exception {

        logger.info("try to star server ,meta info {}", MetaInfo.toMap());
        Executor executor = new ThreadPoolExecutor(MetaInfo.SERVING_CORE_POOL_SIZE, MetaInfo.SERVING_MAX_POOL_SIZE, MetaInfo.SERVING_POOL_ALIVE_TIME, TimeUnit.MILLISECONDS,
                MetaInfo.SERVING_POOL_QUEUE_SIZE == 0 ? new SynchronousQueue<Runnable>() :
                        ( MetaInfo.SERVING_POOL_QUEUE_SIZE < 0 ? new LinkedBlockingQueue<Runnable>()
                                : new LinkedBlockingQueue<Runnable>( MetaInfo.SERVING_POOL_QUEUE_SIZE)), new NamedThreadFactory("ServingServer", true));

        FateServerBuilder serverBuilder = (FateServerBuilder) ServerBuilder.forPort(MetaInfo.PROPERTY_PORT);
        serverBuilder.keepAliveTime(100, TimeUnit.MILLISECONDS);
        serverBuilder.executor(executor);
        serverBuilder.addService(ServerInterceptors.intercept(guestInferenceService, new ServiceExceptionHandler(), new ServiceOverloadProtectionHandle()), GuestInferenceService.class);
        serverBuilder.addService(ServerInterceptors.intercept(modelService, new ServiceExceptionHandler(), new ServiceOverloadProtectionHandle()), ModelService.class);
        serverBuilder.addService(ServerInterceptors.intercept(hostInferenceService, new ServiceExceptionHandler(), new ServiceOverloadProtectionHandle()), HostInferenceService.class);
        serverBuilder.addService(ServerInterceptors.intercept(commonService, new ServiceExceptionHandler(), new ServiceOverloadProtectionHandle()), CommonService.class);
        server = serverBuilder.build();
        server.start();
        boolean useRegister = MetaInfo.PROPERTY_USE_REGISTER;
        if (useRegister) {
            logger.info("serving-server is using register center");
            zookeeperRegistry.subProject(Dict.PROPERTY_PROXY_ADDRESS);
            zookeeperRegistry.subProject(Dict.PROPERTY_FLOW_ADDRESS);
            FateServer.serviceSets.forEach(servie -> {
                try {
                    String serviceName = servie.serviceName();
                    String weightKey = serviceName + ".weight";
                    int weight = environment.getProperty(weightKey, int.class, 0);
                    if (weight > 0) {
                        zookeeperRegistry.getServiceWeightMap().put(weightKey, weight);
                    }
                } catch (Throwable e) {
                    logger.error("parse interface weight error", e);
                }
            });
            zookeeperRegistry.register(FateServer.serviceSets);
            zookeeperRegistry.registerComponent();
        } else {
            logger.warn("serving-server not use register center");
        }
        modelManager.restore(new BaseContext());
        HttpClientPool.initPool();
        logger.warn("serving-server start over");
    }
}
