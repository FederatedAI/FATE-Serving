package com.webank.ai.fate.serving;

import com.google.common.collect.Sets;
import com.webank.ai.fate.register.url.URL;
import com.webank.ai.fate.register.zookeeper.ZookeeperRegistry;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.rpc.core.AbstractServiceAdaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Set;

/**
 * @Description TODO
 * @Author
 **/
@SpringBootApplication
@ComponentScan(basePackages = {"com.webank.ai.fate.serving.*"})
@ConfigurationProperties
//value = "file:${user.dir}/conf/serving-server.properties"
@PropertySource(value = "serving-server.properties",
        ignoreResourceNotFound = false)
@EnableScheduling
public class Bootstrap {

    Logger logger = LoggerFactory.getLogger(Bootstrap.class);

    private static ApplicationContext applicationContext;

    public void start(String[] args) {

        System.err.println("iiiiiiiiiiiiiiiii");
        applicationContext = SpringApplication.run(new Class[]{Bootstrap.class,SpringConfig.class}, args);
    }

    public void stop() {
        logger.info("try to shutdown server ==============!!!!!!!!!!!!!!!!!!!!!");
        AbstractServiceAdaptor.isOpen = false;
        int tryNum = 0;
        /**
         * 3秒
         */
        while (AbstractServiceAdaptor.requestInHandle.get() > 0 && tryNum < 30) {
            logger.info("try to shundown,try count {}, remain {}", tryNum, AbstractServiceAdaptor.requestInHandle.get());
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        boolean useZkRouter = Boolean.parseBoolean(applicationContext.getEnvironment().getProperty(Dict.USE_ZK_ROUTER, "false"));
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

    public static void main(String[] args) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.start(args);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> bootstrap.stop()));
    }

}