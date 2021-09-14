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

import com.webank.ai.fate.serving.common.flow.JvmInfoCounter;
import com.webank.ai.fate.serving.common.rpc.core.AbstractServiceAdaptor;
import com.webank.ai.fate.serving.common.utils.HttpClientPool;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import org.apache.commons.lang3.StringUtils;
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
            MetaInfo.PROPERTY_ROOT_PATH = new File("").getCanonicalPath();
            MetaInfo.PROPERTY_PROXY_ADDRESS = environment.getProperty(Dict.PROPERTY_PROXY_ADDRESS);
            MetaInfo.PROPERTY_SERVING_CORE_POOL_SIZE = environment.getProperty(Dict.PROPERTY_SERVING_CORE_POOL_SIZE) != null ? Integer.valueOf(environment.getProperty(Dict.PROPERTY_SERVING_CORE_POOL_SIZE)) : processors;
            MetaInfo.PROPERTY_SERVING_MAX_POOL_SIZE = environment.getProperty(Dict.PROPERTY_SERVING_MAX_POOL_SIZE) != null ? Integer.valueOf(environment.getProperty(Dict.PROPERTY_SERVING_MAX_POOL_SIZE)) : processors * 2;
            MetaInfo.PROPERTY_SERVING_POOL_ALIVE_TIME = environment.getProperty(Dict.PROPERTY_SERVING_POOL_ALIVE_TIME) != null ? Integer.valueOf(environment.getProperty(Dict.PROPERTY_SERVING_POOL_ALIVE_TIME)) : 1000;
            MetaInfo.PROPERTY_SERVING_POOL_QUEUE_SIZE = environment.getProperty(Dict.PROPERTY_SERVING_POOL_QUEUE_SIZE) != null ? Integer.valueOf(environment.getProperty(Dict.PROPERTY_SERVING_POOL_QUEUE_SIZE)) : 100;
            MetaInfo.PROPERTY_FEATURE_BATCH_ADAPTOR = environment.getProperty(Dict.PROPERTY_FEATURE_BATCH_ADAPTOR);
            MetaInfo.PROPERTY_BATCH_INFERENCE_MAX = environment.getProperty(Dict.PROPERTY_BATCH_INFERENCE_MAX) != null ? Integer.valueOf(environment.getProperty(Dict.PROPERTY_BATCH_INFERENCE_MAX)) : 300;
            MetaInfo.PROPERTY_REMOTE_MODEL_INFERENCE_RESULT_CACHE_SWITCH = environment.getProperty(Dict.PROPERTY_REMOTE_MODEL_INFERENCE_RESULT_CACHE_SWITCH) != null ? Boolean.valueOf(environment.getProperty(Dict.PROPERTY_REMOTE_MODEL_INFERENCE_RESULT_CACHE_SWITCH)) : Boolean.FALSE;
            MetaInfo.PROPERTY_SINGLE_INFERENCE_RPC_TIMEOUT = environment.getProperty(Dict.PROPERTY_SINGLE_INFERENCE_RPC_TIMEOUT) != null ? Integer.valueOf(environment.getProperty(Dict.PROPERTY_SINGLE_INFERENCE_RPC_TIMEOUT)) : 3000;
            MetaInfo.PROPERTY_BATCH_INFERENCE_RPC_TIMEOUT = environment.getProperty(Dict.PROPERTY_BATCH_INFERENCE_RPC_TIMEOUT) != null ? Integer.valueOf(environment.getProperty(Dict.PROPERTY_BATCH_INFERENCE_RPC_TIMEOUT)) : 3000;
            MetaInfo.PROPERTY_FEATURE_SINGLE_ADAPTOR = environment.getProperty(Dict.PROPERTY_FEATURE_SINGLE_ADAPTOR);
            MetaInfo.PROPERTY_USE_REGISTER = environment.getProperty(Dict.PROPERTY_USE_REGISTER) != null ? Boolean.valueOf(environment.getProperty(Dict.PROPERTY_USE_REGISTER)) : true;
            MetaInfo.PROPERTY_USE_ZK_ROUTER = environment.getProperty(Dict.PROPERTY_USE_ZK_ROUTER) != null ? Boolean.valueOf(environment.getProperty(Dict.PROPERTY_USE_ZK_ROUTER)) : true;
            MetaInfo.PROPERTY_SERVER_PORT = environment.getProperty(Dict.PORT) != null ? Integer.valueOf(environment.getProperty(Dict.PORT)) : 8000;
            MetaInfo.PROPERTY_ZK_URL = environment.getProperty(Dict.PROPERTY_ZK_URL);
            MetaInfo.PROPERTY_CACHE_TYPE = environment.getProperty(Dict.PROPERTY_CACHE_TYPE, "local");
            MetaInfo.PROPERTY_REDIS_IP = environment.getProperty(Dict.PROPERTY_REDIS_IP);
            MetaInfo.PROPERTY_REDIS_PASSWORD = environment.getProperty(Dict.PROPERTY_REDIS_PASSWORD);
            MetaInfo.PROPERTY_REDIS_PORT = environment.getProperty(Dict.PROPERTY_REDIS_PORT) != null ? Integer.valueOf(environment.getProperty(Dict.PROPERTY_REDIS_PORT)) : 3306;
            MetaInfo.PROPERTY_REDIS_TIMEOUT = environment.getProperty(Dict.PROPERTY_REDIS_TIMEOUT) != null ? Integer.valueOf(environment.getProperty(Dict.PROPERTY_REDIS_TIMEOUT)) : 2000;
            MetaInfo.PROPERTY_REDIS_MAX_TOTAL = environment.getProperty(Dict.PROPERTY_REDIS_MAX_TOTAL) != null ? Integer.valueOf(environment.getProperty(Dict.PROPERTY_REDIS_MAX_TOTAL)) : 20;
            MetaInfo.PROPERTY_REDIS_MAX_IDLE = environment.getProperty(Dict.PROPERTY_REDIS_MAX_IDLE) != null ? Integer.valueOf(environment.getProperty(Dict.PROPERTY_REDIS_MAX_IDLE)) : 2;
            MetaInfo.PROPERTY_REDIS_EXPIRE = environment.getProperty(Dict.PROPERTY_REDIS_EXPIRE) != null ? Integer.valueOf(environment.getProperty(Dict.PROPERTY_REDIS_EXPIRE)) : 3000;
            MetaInfo.PROPERTY_REDIS_CLUSTER_NODES = environment.getProperty(Dict.PROPERTY_REDIS_CLUSTER_NODES);
            MetaInfo.PROPERTY_LOCAL_CACHE_MAXSIZE = environment.getProperty(Dict.PROPERTY_LOCAL_CACHE_MAXSIZE) != null ? Integer.valueOf(environment.getProperty(Dict.PROPERTY_LOCAL_CACHE_MAXSIZE)) : 10000;
            MetaInfo.PROPERTY_LOCAL_CACHE_EXPIRE = environment.getProperty(Dict.PROPERTY_LOCAL_CACHE_EXPIRE) != null ? Integer.valueOf(environment.getProperty(Dict.PROPERTY_LOCAL_CACHE_EXPIRE)) : 30;
            MetaInfo.PROPERTY_LOCAL_CACHE_INTERVAL = environment.getProperty(Dict.PROPERTY_LOCAL_CACHE_INTERVAL) != null ? Integer.valueOf(environment.getProperty(Dict.PROPERTY_LOCAL_CACHE_INTERVAL)) : 3;
            MetaInfo.PROPERTY_BATCH_SPLIT_SIZE = environment.getProperty(Dict.PROPERTY_BATCH_SPLIT_SIZE) != null ? Integer.valueOf(environment.getProperty(Dict.PROPERTY_BATCH_SPLIT_SIZE)) : 100;
            MetaInfo.PROPERTY_LR_SPLIT_SIZE = environment.getProperty(Dict.PROPERTY_LR_SPLIT_SIZE) != null ? Integer.valueOf(environment.getProperty(Dict.PROPERTY_LR_SPLIT_SIZE)) : 500;
            MetaInfo.PROPERTY_SERVICE_ROLE_NAME = environment.getProperty(Dict.PROPERTY_SERVICE_ROLE_NAME, Dict.PROPERTY_SERVICE_ROLE_NAME_DEFAULT_VALUE);
            MetaInfo.PROPERTY_MODEL_TRANSFER_URL = environment.getProperty(Dict.PROPERTY_MODEL_TRANSFER_URL);
            MetaInfo.PROPERTY_MODEL_CACHE_PATH = StringUtils.isNotBlank(environment.getProperty(Dict.PROPERTY_MODEL_CACHE_PATH)) ? environment.getProperty(Dict.PROPERTY_MODEL_CACHE_PATH) : MetaInfo.PROPERTY_ROOT_PATH;
            MetaInfo.PROPERTY_ACL_ENABLE = Boolean.valueOf(environment.getProperty(Dict.PROPERTY_ACL_ENABLE, "false"));
            MetaInfo.PROPERTY_MODEL_SYNC = Boolean.valueOf(environment.getProperty(Dict.PROPERTY_MODEL_SYNC, "false"));
            MetaInfo.PROPERTY_MODEL_SYNC = Boolean.valueOf(environment.getProperty(Dict.PROPERTY_MODEL_SYNC, "false"));
            MetaInfo.PROPERTY_GRPC_TIMEOUT = Integer.valueOf(environment.getProperty(Dict.PROPERTY_GRPC_TIMEOUT, "5000"));
            MetaInfo.PROPERTY_ACL_USERNAME = environment.getProperty(Dict.PROPERTY_ACL_USERNAME);
            MetaInfo.PROPERTY_ACL_PASSWORD = environment.getProperty(Dict.PROPERTY_ACL_PASSWORD);
            MetaInfo.PROPERTY_PRINT_INPUT_DATA = environment.getProperty(Dict.PROPERTY_PRINT_INPUT_DATA) != null ? Boolean.valueOf(environment.getProperty(Dict.PROPERTY_PRINT_INPUT_DATA)) : false;
            MetaInfo.PROPERTY_PRINT_OUTPUT_DATA = environment.getProperty(Dict.PROPERTY_PRINT_OUTPUT_DATA) != null ? Boolean.valueOf(environment.getProperty(Dict.PROPERTY_PRINT_OUTPUT_DATA)) : false;
            MetaInfo.PROPERTY_LR_USE_PARALLEL = environment.getProperty(Dict.PROPERTY_LR_USE_PARALLEL) != null ? Boolean.valueOf(environment.getProperty(Dict.PROPERTY_LR_USE_PARALLEL)) : false;
            MetaInfo.PROPERTY_ALLOW_HEALTH_CHECK = environment.getProperty(Dict.PROPERTY_ALLOW_HEALTH_CHECK) != null ? Boolean.valueOf(environment.getProperty(Dict.PROPERTY_ALLOW_HEALTH_CHECK)) : true;
            MetaInfo.HTTP_CLIENT_CONFIG_CONN_REQ_TIME_OUT = Integer.valueOf(environment.getProperty(Dict.HTTP_CLIENT_CONFIG_CONN_REQ_TIME_OUT,"500"));
            MetaInfo.HTTP_CLIENT_CONFIG_CONN_TIME_OUT = Integer.valueOf(environment.getProperty(Dict.HTTP_CLIENT_CONFIG_CONN_TIME_OUT,"500"));
            MetaInfo.HTTP_CLIENT_CONFIG_SOCK_TIME_OUT = Integer.valueOf(environment.getProperty(Dict.HTTP_CLIENT_CONFIG_SOCK_TIME_OUT,"2000"));
            MetaInfo.HTTP_CLIENT_INIT_POOL_MAX_TOTAL = Integer.valueOf(environment.getProperty(Dict.HTTP_CLIENT_INIT_POOL_MAX_TOTAL,"500"));
            MetaInfo.HTTP_CLIENT_INIT_POOL_DEF_MAX_PER_ROUTE = Integer.valueOf(environment.getProperty(Dict.HTTP_CLIENT_INIT_POOL_DEF_MAX_PER_ROUTE,"200"));
            MetaInfo.HTTP_CLIENT_INIT_POOL_SOCK_TIME_OUT = Integer.valueOf(environment.getProperty(Dict.HTTP_CLIENT_INIT_POOL_SOCK_TIME_OUT,"10000"));
            MetaInfo.HTTP_CLIENT_INIT_POOL_CONN_TIME_OUT = Integer.valueOf(environment.getProperty(Dict.HTTP_CLIENT_INIT_POOL_CONN_TIME_OUT,"10000"));
            MetaInfo.HTTP_CLIENT_INIT_POOL_CONN_REQ_TIME_OUT = Integer.valueOf(environment.getProperty(Dict.HTTP_CLIENT_INIT_POOL_CONN_REQ_TIME_OUT,"10000"));
            MetaInfo.HTTP_CLIENT_TRAN_CONN_REQ_TIME_OUT = Integer.valueOf(environment.getProperty(Dict.HTTP_CLIENT_TRAN_CONN_REQ_TIME_OUT,"60000"));
            MetaInfo.HTTP_CLIENT_TRAN_CONN_TIME_OUT = Integer.valueOf(environment.getProperty(Dict.HTTP_CLIENT_TRAN_CONN_TIME_OUT,"60000"));
            MetaInfo.HTTP_CLIENT_TRAN_SOCK_TIME_OUT = Integer.valueOf(environment.getProperty(Dict.HTTP_CLIENT_TRAN_SOCK_TIME_OUT,"60000"));
            MetaInfo.PROPERTY_HTTP_CONNECT_REQUEST_TIMEOUT = environment.getProperty(Dict.PROPERTY_HTTP_CONNECT_REQUEST_TIMEOUT) !=null?Integer.parseInt(environment.getProperty(Dict.PROPERTY_HTTP_CONNECT_REQUEST_TIMEOUT)):10000;
            MetaInfo.PROPERTY_HTTP_CONNECT_TIMEOUT = environment.getProperty(Dict.PROPERTY_HTTP_CONNECT_TIMEOUT) !=null?Integer.parseInt(environment.getProperty(Dict.PROPERTY_HTTP_CONNECT_TIMEOUT)):10000;
            MetaInfo.PROPERTY_HTTP_SOCKET_TIMEOUT = environment.getProperty(Dict.PROPERTY_HTTP_SOCKET_TIMEOUT) !=null?Integer.parseInt(environment.getProperty(Dict.PROPERTY_HTTP_SOCKET_TIMEOUT)):10000;
            MetaInfo.PROPERTY_HTTP_MAX_POOL_SIZE = environment.getProperty(Dict.PROPERTY_HTTP_MAX_POOL_SIZE) !=null?Integer.parseInt(environment.getProperty(Dict.PROPERTY_HTTP_MAX_POOL_SIZE)):50;
            MetaInfo.PROPERTY_HTTP_ADAPTER_URL = environment.getProperty(Dict.PROPERTY_HTTP_ADAPTER_URL);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("init metainfo error", e);
            System.exit(1);
        }
    }

    public void start(String[] args) {
        HttpClientPool.initPool();
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