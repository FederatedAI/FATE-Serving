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

package com.webank.ai.fate.serving.admin;

import com.webank.ai.fate.register.zookeeper.ZookeeperRegistry;
import com.webank.ai.fate.serving.common.flow.JvmInfoCounter;
import com.webank.ai.fate.serving.common.rpc.core.AbstractServiceAdaptor;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
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
            logger.error("serving-admin start error", ex);
            System.exit(1);
        }
    }

    public static void parseConfig() {
        ClassPathResource classPathResource = new ClassPathResource("application.properties");
        try {
            File file = classPathResource.getFile();
            Properties environment = new Properties();
            try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
                environment.load(inputStream);
            } catch (FileNotFoundException e) {
                logger.error("profile application.properties not found");
            } catch (IOException e) {
                logger.error("parse config error, {}", e.getMessage());
            }
            // serving-admin must open the zookeeper registry
            MetaInfo.PROPERTY_USE_ZK_ROUTER = true;
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
            MetaInfo.PROPERTY_LOCAL_CACHE_EXPIRE = environment.getProperty(Dict.PROPERTY_LOCAL_CACHE_EXPIRE) != null ? Integer.valueOf(environment.getProperty(Dict.PROPERTY_LOCAL_CACHE_EXPIRE)) : 300;
            MetaInfo.PROPERTY_LOCAL_CACHE_INTERVAL = environment.getProperty(Dict.PROPERTY_LOCAL_CACHE_INTERVAL) != null ? Integer.valueOf(environment.getProperty(Dict.PROPERTY_LOCAL_CACHE_INTERVAL)) : 3;
            MetaInfo.PROPERTY_FATEFLOW_LOAD_URL = environment.getProperty(Dict.PROPERTY_FATEFLOW_LOAD_URL);
            MetaInfo.PROPERTY_FATEFLOW_BIND_URL = environment.getProperty(Dict.PROPERTY_FATEFLOW_LOAD_URL);
            MetaInfo.PROPERTY_GRPC_TIMEOUT = Integer.valueOf(environment.getProperty(Dict.PROPERTY_GRPC_TIMEOUT, "5000"));
            MetaInfo.PROPERTY_ACL_ENABLE = Boolean.valueOf(environment.getProperty(Dict.PROPERTY_ACL_ENABLE, "false"));
            MetaInfo.PROPERTY_ACL_USERNAME = environment.getProperty(Dict.PROPERTY_ACL_USERNAME);
            MetaInfo.PROPERTY_ACL_PASSWORD = environment.getProperty(Dict.PROPERTY_ACL_PASSWORD);
            MetaInfo.PROPERTY_PRINT_INPUT_DATA = environment.getProperty(Dict.PROPERTY_PRINT_INPUT_DATA) != null ? Boolean.valueOf(environment.getProperty(Dict.PROPERTY_PRINT_INPUT_DATA)) : false;
            MetaInfo.PROPERTY_PRINT_OUTPUT_DATA = environment.getProperty(Dict.PROPERTY_PRINT_OUTPUT_DATA) != null ? Boolean.valueOf(environment.getProperty(Dict.PROPERTY_PRINT_OUTPUT_DATA)) : false;
            MetaInfo.PROPERTY_ADMIN_HEALTH_CHECK_TIME = environment.getProperty(Dict.PROPERTY_ADMIN_HEALTH_CHECK_TIME) != null ? Integer.valueOf(environment.getProperty(Dict.PROPERTY_ADMIN_HEALTH_CHECK_TIME)) : 30;
            MetaInfo.PROPERTY_ALLOW_HEALTH_CHECK = environment.getProperty(Dict.PROPERTY_ALLOW_HEALTH_CHECK) != null ? Boolean.valueOf(environment.getProperty(Dict.PROPERTY_ALLOW_HEALTH_CHECK)) : true;

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("init meta info error", e);
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
}
