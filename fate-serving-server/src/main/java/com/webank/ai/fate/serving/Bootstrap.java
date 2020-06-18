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
import com.webank.ai.fate.serving.common.flow.JvmInfoCounter;
import com.webank.ai.fate.serving.common.rpc.core.AbstractServiceAdaptor;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.*;
import java.util.Properties;
import java.util.Set;

@SpringBootApplication
@ConfigurationProperties
@PropertySource(value = "classpath:serving-server.properties", ignoreResourceNotFound = false)
@EnableScheduling
public class Bootstrap {
    static Logger logger = LoggerFactory.getLogger(Bootstrap.class);
    private static ApplicationContext applicationContext;
    public static void main(String[] args) {
        try {
            parseConfig();
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.start(args);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> bootstrap.stop()));
        } catch (Exception ex) {
            logger.error("serving-server start error", ex);
            System.exit(1);
        }
    }

    public static void parseConfig() {
        ClassPathResource classPathResource = new ClassPathResource("serving-server.properties");
        try {
            File file = classPathResource.getFile();
            Properties environment = new Properties();
            try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
                environment.load(inputStream);
            } catch (FileNotFoundException e) {
                logger.error("profile serving-server.properties not found");
                throw e;
            } catch (IOException e) {
                logger.error("parse config error, {}", e.getMessage());
                throw e;
            }
            int processors = Runtime.getRuntime().availableProcessors();
            MetaInfo.PROPERTY_PROXY_ADDRESS = environment.getProperty(Dict.PROPERTY_PROXY_ADDRESS);
            MetaInfo.SERVING_CORE_POOL_SIZE = environment.getProperty(Dict.SERVING_CORE_POOL_SIZE) != null ? Integer.valueOf(environment.getProperty(Dict.SERVING_CORE_POOL_SIZE)) : processors;
            MetaInfo.SERVING_MAX_POOL_SIZE = environment.getProperty(Dict.SERVING_MAX_POOL_SIZE) != null ? Integer.valueOf(environment.getProperty(Dict.SERVING_MAX_POOL_SIZE)) : processors * 2;
            MetaInfo.SERVING_POOL_ALIVE_TIME = environment.getProperty(Dict.SERVING_POOL_ALIVE_TIME) != null ? Integer.valueOf(environment.getProperty(Dict.SERVING_POOL_ALIVE_TIME)) : 1000;
            MetaInfo.SERVING_POOL_QUEUE_SIZE = environment.getProperty(Dict.SERVING_POOL_QUEUE_SIZE) != null ? Integer.valueOf(environment.getProperty(Dict.SERVING_POOL_QUEUE_SIZE)) : 100;
            MetaInfo.FEATURE_BATCH_ADAPTOR = environment.getProperty(Dict.FEATURE_BATCH_ADAPTOR);
            MetaInfo.BATCH_INFERENCE_MAX = environment.getProperty(Dict.BATCH_INFERENCE_MAX)!=null? Integer.valueOf(environment.getProperty(Dict.BATCH_INFERENCE_MAX)):300;
            MetaInfo.PROPERTY_REMOTE_MODEL_INFERENCE_RESULT_CACHE_SWITCH = environment.getProperty(Dict.PROPERTY_REMOTE_MODEL_INFERENCE_RESULT_CACHE_SWITCH) != null ? Boolean.valueOf(environment.getProperty(Dict.PROPERTY_REMOTE_MODEL_INFERENCE_RESULT_CACHE_SWITCH)) : Boolean.FALSE;
            MetaInfo.SINGLE_INFERENCE_RPC_TIMEOUT = environment.getProperty(Dict.SINGLE_INFERENCE_RPC_TIMEOUT) != null ? Integer.valueOf(environment.getProperty(Dict.SINGLE_INFERENCE_RPC_TIMEOUT)) : 3000;
            MetaInfo.BATCH_INFERENCE_RPC_TIMEOUT = environment.getProperty(Dict.BATCH_INFERENCE_RPC_TIMEOUT) != null ? Integer.valueOf(environment.getProperty(Dict.BATCH_INFERENCE_RPC_TIMEOUT)) : 3000;
            MetaInfo.FEATURE_SINGLE_ADAPTOR = environment.getProperty(Dict.FEATURE_SINGLE_ADAPTOR);
            MetaInfo.PROPERTY_USE_REGISTER = environment.getProperty(Dict.PROPERTY_USE_REGISTER) != null ? Boolean.valueOf(environment.getProperty(Dict.PROPERTY_USE_REGISTER)) : true;
            MetaInfo.PROPERTY_USE_ZK_ROUTER = environment.getProperty(Dict.PROPERTY_USE_ZK_ROUTER) != null ? Boolean.valueOf(environment.getProperty(Dict.PROPERTY_USE_ZK_ROUTER)) : true;
            MetaInfo.PROPERTY_PORT = environment.getProperty(Dict.PORT) != null ? Integer.valueOf(environment.getProperty(Dict.PORT)) : 8000;
            MetaInfo.PROPERTY_ZK_URL = environment.getProperty(Dict.PROPERTY_ZK_URL);
            MetaInfo.PROPERTY_CACHE_TYPE = environment.getProperty(Dict.PROPERTY_CACHE_TYPE, "local");
            MetaInfo.PROPERTY_REDIS_IP = environment.getProperty(Dict.PROPERTY_REDIS_IP);
            MetaInfo.PROPERTY_REDIS_PASSWORD = environment.getProperty(Dict.PROPERTY_REDIS_PASSWORD);
            MetaInfo.PROPERTY_REDIS_PORT = environment.getProperty(Dict.PROPERTY_REDIS_PORT) != null ? Integer.valueOf(environment.getProperty(Dict.PROPERTY_REDIS_PORT)) : 3306;
            MetaInfo.PROPERTY_REDIS_TIMEOUT = environment.getProperty(Dict.PROPERTY_REDIS_TIMEOUT) != null ? Integer.valueOf(environment.getProperty(Dict.PROPERTY_REDIS_TIMEOUT)) : 2000;
            MetaInfo.PROPERTY_REDIS_MAX_TOTAL = environment.getProperty(Dict.PROPERTY_REDIS_MAX_TOTAL) != null ? Integer.valueOf(environment.getProperty(Dict.PROPERTY_REDIS_MAX_TOTAL)) : 20;
            MetaInfo.PROPERTY_REDIS_MAX_IDLE = environment.getProperty(Dict.PROPERTY_REDIS_MAX_IDLE) != null ? Integer.valueOf(environment.getProperty(Dict.PROPERTY_REDIS_MAX_IDLE)) : 2;
            MetaInfo.PROPERTY_REDIS_EXPIRE = environment.getProperty(Dict.PROPERTY_REDIS_EXPIRE) != null ? Integer.valueOf(environment.getProperty(Dict.PROPERTY_REDIS_EXPIRE)) : 3000;
            MetaInfo.PROPERTY_LOCAL_CACHE_MAXSIZE = environment.getProperty(Dict.PROPERTY_LOCAL_CACHE_MAXSIZE) != null ? Integer.valueOf(environment.getProperty(Dict.PROPERTY_LOCAL_CACHE_MAXSIZE)) : 10000;
            MetaInfo.PROPERTY_LOCAL_CACHE_EXPIRE = environment.getProperty(Dict.PROPERTY_LOCAL_CACHE_EXPIRE) != null ? Integer.valueOf(environment.getProperty(Dict.PROPERTY_LOCAL_CACHE_EXPIRE)) : 30;
            MetaInfo.PROPERTY_LOCAL_CACHE_INTERVAL = environment.getProperty(Dict.PROPERTY_LOCAL_CACHE_INTERVAL) != null ? Integer.valueOf(environment.getProperty(Dict.PROPERTY_LOCAL_CACHE_INTERVAL)) : 3;
            MetaInfo.BATCH_SPLIT_SIZE = environment.getProperty(Dict.BATCH_SPLIT_SIZE) != null ? Integer.valueOf(environment.getProperty(Dict.BATCH_SPLIT_SIZE)) : 100;
            MetaInfo.LR_SPLIT_SIZE = environment.getProperty(Dict.LR_SPLIT_SIZE) != null ? Integer.valueOf(environment.getProperty(Dict.LR_SPLIT_SIZE)) : 500;
            MetaInfo.PROPERTY_SERVICE_ROLE_NAME = environment.getProperty(Dict.PROPERTY_SERVICE_ROLE_NAME, Dict.PROPERTY_SERVICE_ROLE_NAME_DEFAULT_VALUE);
            MetaInfo.MODEL_TRANSFER_URL = environment.getProperty(Dict.MODEL_TRANSFER_URL);


        } catch (IOException e) {
            logger.error("init metainfo error", e);
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

        int retryCount = 0;
        while (AbstractServiceAdaptor.requestInHandle.get() > 0 && retryCount < 30) {
            logger.info("try to stop server, there is {} request in process, try count {}", AbstractServiceAdaptor.requestInHandle.get(), retryCount + 1);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            retryCount++;
        }
    }

}