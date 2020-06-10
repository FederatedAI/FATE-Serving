package com.webank.ai.fate.serving.admin;

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
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.util.Properties;

@SpringBootApplication
@PropertySource("classpath:application.properties")
public class Bootstrap {

    private static Logger logger = LoggerFactory.getLogger(Bootstrap.class);

    private static ApplicationContext applicationContext;

    public static void main(String[] args) {
        try {
            parseConfig();
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.start(args);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> bootstrap.stop()));
        } catch (Exception ex) {
            logger.error("serving-admin start error",ex);
            System.exit(1);
        }
    }

    public void start(String[] args) {
        SpringApplication springApplication = new SpringApplication(Bootstrap.class);
        applicationContext = springApplication.run(args);
        JvmInfoCounter.start();
    }

    public void stop() {
        logger.info("try to shutdown server ...");
        AbstractServiceAdaptor.isOpen = false;

        boolean useZkRouter = MetaInfo.PROPERTY_USE_ZK_ROUTER;
        if (useZkRouter) {
            ZookeeperRegistry zookeeperRegistry = applicationContext.getBean(ZookeeperRegistry.class);
            zookeeperRegistry.destroy();
        }

        int tryNum = 0;
        while (AbstractServiceAdaptor.requestInHandle.get() > 0 && tryNum < 30) {
            logger.info("try to shutdown,try count {}, remain {}", tryNum, AbstractServiceAdaptor.requestInHandle.get());
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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

            MetaInfo.PROPERTY_USE_ZK_ROUTER = environment.getProperty(Dict.PROPERTY_USE_ZK_ROUTER)!=null?Boolean.valueOf(environment.getProperty(Dict.PROPERTY_USE_ZK_ROUTER)):true;
            MetaInfo.PROPERTY_SERVER_PORT = environment.getProperty(Dict.PROPERTY_SERVER_PORT) != null ? Integer.valueOf(environment.getProperty(Dict.PROPERTY_SERVER_PORT)) : 8350;
            MetaInfo.PROPERTY_ZK_URL = environment.getProperty(Dict.PROPERTY_ZK_URL);

            MetaInfo.PROPERTY_CACHE_TYPE = environment.getProperty(Dict.PROPERTY_CACHE_TYPE, "local");
            MetaInfo.PROPERTY_REDIS_IP = environment.getProperty(Dict.PROPERTY_REDIS_IP);
            MetaInfo.PROPERTY_REDIS_PASSWORD = environment.getProperty(Dict.PROPERTY_REDIS_PASSWORD);
            MetaInfo.PROPERTY_REDIS_PORT = environment.getProperty(Dict.PROPERTY_REDIS_PORT) != null ? Integer.valueOf(environment.getProperty(Dict.PROPERTY_REDIS_PORT)) : 6379;
            MetaInfo.PROPERTY_REDIS_TIMEOUT = environment.getProperty(Dict.PROPERTY_REDIS_TIMEOUT) != null ? Integer.valueOf(environment.getProperty(Dict.PROPERTY_REDIS_TIMEOUT)) : 2000;
            MetaInfo.PROPERTY_REDIS_MAX_TOTAL = environment.getProperty(Dict.PROPERTY_REDIS_MAX_TOTAL) != null ? Integer.valueOf(environment.getProperty(Dict.PROPERTY_REDIS_MAX_TOTAL)) : 20;
            MetaInfo.PROPERTY_REDIS_MAX_IDLE = environment.getProperty(Dict.PROPERTY_REDIS_MAX_IDLE) != null ? Integer.valueOf(environment.getProperty(Dict.PROPERTY_REDIS_MAX_IDLE)) : 2;
            MetaInfo.PROPERTY_REDIS_EXPIRE = environment.getProperty(Dict.PROPERTY_REDIS_EXPIRE) != null ? Integer.valueOf(environment.getProperty(Dict.PROPERTY_REDIS_EXPIRE)) : 3000;
            MetaInfo.PROPERTY_LOCAL_CACHE_MAXSIZE = environment.getProperty(Dict.PROPERTY_LOCAL_CACHE_MAXSIZE) != null ? Integer.valueOf(environment.getProperty(Dict.PROPERTY_LOCAL_CACHE_MAXSIZE)) : 10000;
            MetaInfo.PROPERTY_LOCAL_CACHE_EXPIRE = environment.getProperty(Dict.PROPERTY_LOCAL_CACHE_EXPIRE) != null ? Integer.valueOf(environment.getProperty(Dict.PROPERTY_LOCAL_CACHE_EXPIRE)) : 30;
            MetaInfo.PROPERTY_LOCAL_CACHE_INTERVAL = environment.getProperty(Dict.PROPERTY_LOCAL_CACHE_INTERVAL) != null ? Integer.valueOf(environment.getProperty(Dict.PROPERTY_LOCAL_CACHE_INTERVAL)) : 3;

        } catch (IOException e) {
            logger.error("init meta info error", e);
        }

    }
}
