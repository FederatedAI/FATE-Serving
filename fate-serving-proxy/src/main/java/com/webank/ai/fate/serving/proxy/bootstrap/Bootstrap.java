package com.webank.ai.fate.serving.proxy.bootstrap;

import com.google.common.collect.Sets;
import com.webank.ai.fate.register.url.URL;
import com.webank.ai.fate.register.zookeeper.ZookeeperRegistry;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.flow.JvmInfoCounter;
import com.webank.ai.fate.serving.core.rpc.core.AbstractServiceAdaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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
@ComponentScan(basePackages = {"com.webank.ai.fate.serving.proxy.*"})
@PropertySource("classpath:application.properties")
@EnableScheduling
public class Bootstrap {

    private static ApplicationContext applicationContext;
    Logger logger = LoggerFactory.getLogger(Bootstrap.class);

    public static void main(String[] args) {
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.start(args);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> bootstrap.stop()));
        } catch (Exception ex) {
            System.err.println("server start error, " + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
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
            logger.info("try to shundown,try count {}, remain {}", tryNum, AbstractServiceAdaptor.requestInHandle.get());
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        boolean useZkRouter = applicationContext.getEnvironment().getProperty(Dict.USE_ZK_ROUTER, boolean.class, Boolean.TRUE);
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