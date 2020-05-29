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
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
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

        SpringApplication springApplication = new SpringApplication(Bootstrap.class);
        springApplication.addListeners(new ApplicationListener<ApplicationEnvironmentPreparedEvent>() {
            @Override
            public void onApplicationEvent(ApplicationEnvironmentPreparedEvent applicationReadyEvent) {

                ConfigurableEnvironment environment = applicationReadyEvent.getEnvironment();

                int processors = Runtime.getRuntime().availableProcessors();

                MetaInfo.PROPERTY_PROXY_ADDRESS = environment.getProperty(Dict.PROPERTY_PROXY_ADDRESS);
                MetaInfo.SERVING_CORE_POOL_SIZE = environment.getProperty(Dict.SERVING_CORE_POOL_SIZE, int.class, processors);
                MetaInfo.SERVING_MAX_POOL_SIZE = environment.getProperty(Dict.SERVING_MAX_POOL_SIZE, int.class, processors * 2);
                MetaInfo.SERVING_POOL_ALIVE_TIME = environment.getProperty(Dict.SERVING_POOL_ALIVE_TIME, int.class, 1000);
                MetaInfo.USE_REGISTER = environment.getProperty(Dict.USE_REGISTER, boolean.class, Boolean.TRUE);
                MetaInfo.FEATURE_BATCH_ADAPTOR = environment.getProperty(Dict.FEATURE_BATCH_ADAPTOR);
                MetaInfo.PROPERTY_REMOTE_MODEL_INFERENCE_RESULT_CACHE_SWITCH = environment.getProperty(Dict.PROPERTY_REMOTE_MODEL_INFERENCE_RESULT_CACHE_SWITCH, boolean.class, Boolean.FALSE);
                MetaInfo.SINGLE_INFERENCE_RPC_TIMEOUT = environment.getProperty(Dict.SINGLE_INFERENCE_RPC_TIMEOUT, Integer.class, 3000);
                MetaInfo.BATCH_INFERENCE_RPC_TIMEOUT = environment.getProperty(Dict.BATCH_INFERENCE_RPC_TIMEOUT, Integer.class, 3000);
                MetaInfo.FEATURE_SINGLE_ADAPTOR = environment.getProperty(Dict.FEATURE_SINGLE_ADAPTOR);
                MetaInfo.PORT = environment.getProperty(Dict.PORT, Integer.class, 8000);

//         boolean useRegister = environment.getProperty(Dict.USE_REGISTER, boolean.class, Boolean.TRUE);
//
//    String ip = environment.getProperty("redis.ip");
//    String password = environment.getProperty("redis.password");
//    Integer port = environment.getProperty("redis.port", Integer.class);
//    Integer timeout = environment.getProperty("redis.timeout", Integer.class, 2000);
//    Integer maxTotal = environment.getProperty("redis.maxTotal", Integer.class, 20);
//    Integer maxIdle = environment.getProperty("redis.maxIdle", Integer.class, 20);
//    Integer expire = environment.getProperty("redis.expire", Integer.class);
//
//    Integer maxSize = environment.getProperty("local.cache.maxsize", Integer.class, 10000);
//    Integer expireTime = environment.getProperty("local.cache.expire", Integer.class, 30);
//    Integer interval = environment.getProperty("local.cache.interval", Integer.class, 3);


            }
        });
        applicationContext = springApplication.run(args);


        // applicationContext = SpringApplication.run(new Class[]{Bootstrap.class}, args);
    }

    public void stop() {
        logger.info("try to shutdown server ...");
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