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

package com.webank.ai.fate.serving.proxy.bootstrap;

import com.webank.ai.fate.serving.common.flow.JvmInfoCounter;
import com.webank.ai.fate.serving.common.rpc.core.AbstractServiceAdaptor;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
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

            MetaInfo.PROPERTY_COORDINATOR = Integer.valueOf(environment.getProperty(Dict.PROPERTY_COORDINATOR, "9999"));
            MetaInfo.PROPERTY_SERVER_PORT = Integer.valueOf(environment.getProperty(Dict.PROPERTY_SERVER_PORT, "8059"));
            MetaInfo.PROPERTY_INFERENCE_SERVICE_NAME = environment.getProperty(Dict.PROPERTY_INFERENCE_SERVICE_NAME, "serving");
            MetaInfo.PROPERTY_ROUTE_TYPE = environment.getProperty(Dict.PROPERTY_ROUTE_TYPE, "random");
            MetaInfo.PROPERTY_ROUTE_TABLE = environment.getProperty(Dict.PROPERTY_ROUTE_TABLE);
            MetaInfo.PROPERTY_AUTH_FILE = environment.getProperty(Dict.PROPERTY_AUTH_FILE);
            MetaInfo.PROPERTY_AUTH_OPEN = Boolean.valueOf(environment.getProperty(Dict.PROPERTY_AUTH_OPEN, "false"));
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
            MetaInfo.PROPERTY_ACL_ENABLE = Boolean.valueOf(environment.getProperty(Dict.PROPERTY_ACL_ENABLE, "false"));
            MetaInfo.PROPERTY_ACL_USERNAME = environment.getProperty(Dict.PROPERTY_ACL_USERNAME);
            MetaInfo.PROPERTY_ACL_PASSWORD = environment.getProperty(Dict.PROPERTY_ACL_PASSWORD);
            MetaInfo.PROPERTY_PRINT_INPUT_DATA = environment.getProperty(Dict.PROPERTY_PRINT_INPUT_DATA) != null ? Boolean.valueOf(environment.getProperty(Dict.PROPERTY_PRINT_INPUT_DATA)) : false;
            MetaInfo.PROPERTY_PRINT_OUTPUT_DATA = environment.getProperty(Dict.PROPERTY_PRINT_OUTPUT_DATA) != null ? Boolean.valueOf(environment.getProperty(Dict.PROPERTY_PRINT_OUTPUT_DATA)) : false;
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
        applicationContext = SpringApplication.run(Bootstrap.class, args);
        JvmInfoCounter.start();
    }

    public void stop() {
        logger.info("try to shutdown server ");
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