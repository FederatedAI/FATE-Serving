package com.webank.ai.fate.serving.proxy.rpc.grpc;
import com.webank.ai.fate.register.provider.FateServer;
import com.webank.ai.fate.register.provider.FateServerBuilder;
import com.webank.ai.fate.register.router.DefaultRouterService;
import com.webank.ai.fate.register.zookeeper.ZookeeperRegistry;
import com.webank.ai.fate.serving.proxy.common.Dict;
import com.webank.ai.fate.serving.proxy.rpc.router.ZkServingRouter;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import org.apache.commons.lang3.StringUtils;
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

    @Value("${zk.url:zookeeper://localhost:2181}")
    private  String  zkUrl ;

    @Value("${useZkRouter:false}")
    private  String  useZkRouter;

    @Value("${acl.username:fate}")
    private String aclUsername;

    @Value("${acl.password:fate}")
    private String aclPassword;

    ZookeeperRegistry  zookeeperRegistry;

    Logger logger  = LoggerFactory.getLogger(InterGrpcServer.class);

    Server server ;

    @Autowired
    IntraRequestHandler intraRequestHandler;

    @Resource(name="grpcExecutorPool")
    Executor executor;

    @Override
    public void afterPropertiesSet() throws Exception {
        FateServerBuilder serverBuilder = (FateServerBuilder)ServerBuilder.forPort(port);
        serverBuilder.executor(executor);
        serverBuilder.addService(ServerInterceptors.intercept(intraRequestHandler, new ServiceExceptionHandler()));
        serverBuilder.addService(intraRequestHandler);
        server = serverBuilder.build();
        server.start();

        if("true".equals(useZkRouter)&& StringUtils.isNotEmpty(zkUrl)) {

            System.setProperty("acl.username", aclUsername);
            System.setProperty("acl.password", aclPassword);

            zookeeperRegistry = ZookeeperRegistry.getRegistery(zkUrl, Dict.SELF_PROJECT_NAME, Dict.SELF_ENVIRONMENT, Integer.valueOf(port));
            zookeeperRegistry.register(FateServer.serviceSets);
            zookeeperRegistry.subProject("serving");

            DefaultRouterService defaultRouterService = new DefaultRouterService();

            defaultRouterService.setRegistry(zookeeperRegistry);

            ZkServingRouter.setZkRouterService(defaultRouterService);
        }

    }
}

