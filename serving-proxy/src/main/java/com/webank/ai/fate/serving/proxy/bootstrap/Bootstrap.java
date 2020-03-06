package com.webank.ai.fate.serving.proxy.bootstrap;

import com.webank.ai.fate.register.zookeeper.ZookeeperRegistry;
import com.webank.ai.fate.serving.core.bean.BaseContext;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.proxy.rpc.grpc.InterGrpcServer;
import com.webank.ai.fate.serving.proxy.rpc.grpc.IntraGrpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Description TODO
 * @Author
 **/
@SpringBootApplication
@ComponentScan(basePackages = {"com.webank.ai.fate.serving.proxy.*"})
@PropertySource("classpath:application.properties")
@EnableScheduling
public class Bootstrap {

    Logger logger = LoggerFactory.getLogger(Bootstrap.class);

    private static ApplicationContext applicationContext;

    public void start(String[] args) {
        applicationContext = SpringApplication.run(Bootstrap.class, args);
    }

    public void stop() {
        logger.info("try to shutdown server ==============!!!!!!!!!!!!!!!!!!!!!");
        boolean useZkRouter = Boolean.parseBoolean(applicationContext.getEnvironment().getProperty(Dict.USE_ZK_ROUTER, "false"));
        if (useZkRouter) {
            ZookeeperRegistry zookeeperRegistry = applicationContext.getBean(ZookeeperRegistry.class);
            zookeeperRegistry.destroy();
            int retryCount = 0;
            long requestInProcess = BaseContext.requestInProcess.get();
            do {

                logger.info("try to stop server,there is {} request in process,try count {}", requestInProcess, retryCount + 1);
                if (requestInProcess > 0 && retryCount < 30) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    retryCount++;
                    requestInProcess = BaseContext.requestInProcess.get();
                } else {
                    break;
                }

            } while (requestInProcess > 0 && retryCount < 3);

            IntraGrpcServer intraGrpcServer = applicationContext.getBean(IntraGrpcServer.class);
            intraGrpcServer.getServer().shutdown();

            InterGrpcServer interGrpcServer = applicationContext.getBean(InterGrpcServer.class);
            interGrpcServer.getServer().shutdown();
        }
    }

    public static void main(String[] args) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.start(args);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> bootstrap.stop()));
    }

}