package com.webank.ai.fate.serving.proxy.bootstrap;

import com.google.common.collect.Sets;
import com.webank.ai.fate.register.url.URL;
import com.webank.ai.fate.register.zookeeper.ZookeeperRegistry;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import com.webank.ai.fate.serving.core.flow.JvmInfoCounter;
import com.webank.ai.fate.serving.core.rpc.core.AbstractServiceAdaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.*;
import java.util.Properties;
import java.util.Set;

/**
 * @Description TODO
 * @Author
 **/
@SpringBootApplication
@ComponentScan(basePackages = {"com.webank.ai.fate.serving.proxy.*"})
@PropertySource("classpath:application.properties")
@EnableScheduling
public class Bootstrap {

    private static ApplicationContext applicationContext;
    private static Logger logger = LoggerFactory.getLogger(Bootstrap.class);

    public static void main(String[] args) {
        try {
            parseConfig();
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.start(args);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> bootstrap.stop()));
        } catch (Exception ex) {
            System.err.println("server start error, " + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }
    }

    public static void  parseConfig(){

        ClassPathResource classPathResource = new ClassPathResource("application.properties");
        try {
            File file = classPathResource.getFile();
            Properties environment = new Properties();
            try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))){
                environment.load(inputStream);
            } catch (FileNotFoundException e) {
                logger.error("profile application.properties not found");
            } catch (IOException e) {
                logger.error("parse config error, {}", e.getMessage());
            }

            MetaInfo.PROPERTY_COORDINATOR = Integer.valueOf(environment.getProperty(Dict.PROPERTY_COORDINATOR, "9999"));
            MetaInfo.PROPERTY_SERVER_PORT = Integer.valueOf(environment.getProperty(Dict.PROPERTY_SERVER_PORT, "8059"));
            MetaInfo.PROPERTY_INFERENCE_SERVICE_NAME = environment.getProperty(Dict.PROPERTY_INFERENCE_SERVICE_NAME, "serving");
            MetaInfo.PROPERTY_ROUTE_TYPE = environment.getProperty(Dict.PROPERTY_ROUTE_TYPE, "random");
            MetaInfo.PROPERTY_ROUTE_TABLE = environment.getProperty(Dict.PROPERTY_ROUTE_TABLE);
            MetaInfo.PROPERTY_AUTH_FILE = environment.getProperty(Dict.PROPERTY_AUTH_FILE);
            MetaInfo.PROPERTY_ZK_URL = environment.getProperty(Dict.PROPERTY_ZK_URL);
            MetaInfo.PROPERTY_USE_ZK_ROUTER = Boolean.valueOf(environment.getProperty(Dict.PROPERTY_USE_ZK_ROUTER, "true"));
            MetaInfo.PROPERTY_PROXY_GRPC_INTRA_PORT = Integer.valueOf(environment.getProperty(Dict.PROPERTY_PROXY_GRPC_INTRA_PORT, "8879"));
            MetaInfo.PROPERTY_PROXY_GRPC_INTER_PORT = Integer.valueOf(environment.getProperty(Dict.PROPERTY_PROXY_GRPC_INTER_PORT, "8869"));
            MetaInfo.PROPERTY_PROXY_GRPC_INFERENCE_TIMEOUT = Integer.valueOf(environment.getProperty(Dict.PROPERTY_PROXY_GRPC_INFERENCE_TIMEOUT, "3000"));
            MetaInfo.PROPERTY_PROXY_GRPC_INFERENCE_ASYNC_TIMEOUT = Integer.valueOf(environment.getProperty(Dict.PROPERTY_PROXY_GRPC_INFERENCE_ASYNC_TIMEOUT, "1000"));
            MetaInfo.PROPERTY_PROXY_GRPC_UNARYCALL_TIMEOUT = Integer.valueOf(environment.getProperty(Dict.PROPERTY_PROXY_GRPC_UNARYCALL_TIMEOUT, "3000"));
            MetaInfo.PROPERTY_PROXY_GRPC_THREADPOOL_CORESIZE = Integer.valueOf(environment.getProperty(Dict.PROPERTY_PROXY_GRPC_THREADPOOL_CORESIZE, "50"));
            MetaInfo.PROPERTY_PROXY_GRPC_THREADPOOL_MAXSIZE = Integer.valueOf(environment.getProperty(Dict.PROPERTY_PROXY_GRPC_THREADPOOL_MAXSIZE, "100"));
            MetaInfo.PROPERTY_PROXY_GRPC_THREADPOOL_QUEUESIZE = Integer.valueOf(environment.getProperty(Dict.PROPERTY_PROXY_GRPC_THREADPOOL_QUEUESIZE, "10"));
            MetaInfo.PROPERTY_PROXY_ASYNC_TIMEOUT = Integer.valueOf(environment.getProperty(Dict.PROPERTY_PROXY_ASYNC_TIMEOUT, "5000"));
            MetaInfo.PROPERTY_PROXY_ASYNC_CORESIZE = Integer.valueOf(environment.getProperty(Dict.PROPERTY_PROXY_ASYNC_CORESIZE, "10"));
            MetaInfo.PROPERTY_PROXY_ASYNC_MAXSIZE = Integer.valueOf(environment.getProperty(Dict.PROPERTY_PROXY_ASYNC_MAXSIZE, "100"));
            MetaInfo.PROPERTY_PROXY_GRPC_BATCH_INFERENCE_TIMEOUT = Integer.valueOf(environment.getProperty(Dict.PROPERTY_PROXY_GRPC_BATCH_INFERENCE_TIMEOUT, "10000"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void start(String[] args) {
        applicationContext = SpringApplication.run(Bootstrap.class, args);
        JvmInfoCounter.start();
    }

    public void stop() {
        logger.info("try to shutdown server ");
        AbstractServiceAdaptor.isOpen = false;
        int tryNum = 0;
        /**
         * 3秒
         */
        while (AbstractServiceAdaptor.requestInHandle.get() > 0 && tryNum < 30) {
            logger.info("try to shutdown,try count {}, remain {}", tryNum, AbstractServiceAdaptor.requestInHandle.get());
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        boolean useZkRouter = MetaInfo.PROPERTY_USE_ZK_ROUTER;
        if (useZkRouter) {
            ZookeeperRegistry zookeeperRegistry = applicationContext.getBean(ZookeeperRegistry.class);
            Set<URL> registered = zookeeperRegistry.getRegistered();
            Set<URL> urls = Sets.newHashSet();
            urls.addAll(registered);
            urls.forEach(url -> {
                logger.info("unregister {}", url);
                zookeeperRegistry.unregister(url);
            });

            zookeeperRegistry.destroy();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}