/*
 * Copyright 2019 The FATE Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Set;

@SpringBootApplication
@ConfigurationProperties
@PropertySource(value = "classpath:serving-server.properties", ignoreResourceNotFound = false)
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

        applicationContext = SpringApplication.run(new Class[]{Bootstrap.class}, args);
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

}