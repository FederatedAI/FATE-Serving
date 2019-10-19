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

package com.webank.ai.fate.networking;

import com.google.common.collect.Sets;
import com.webank.ai.fate.jmx.server.FateMBeanServer;
import com.webank.ai.fate.networking.proxy.factory.GrpcServerFactory;
import com.webank.ai.fate.networking.proxy.factory.LocalBeanFactory;
import com.webank.ai.fate.networking.proxy.manager.ServerConfManager;
import com.webank.ai.fate.networking.proxy.model.ServerConf;
import com.webank.ai.fate.register.provider.FateServer;
import com.webank.ai.fate.register.url.URL;
import com.webank.ai.fate.register.zookeeper.ZookeeperRegistry;
import io.grpc.Server;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.lang.management.ManagementFactory;
import java.util.Properties;
import java.util.Set;

public class Proxy {
    private static final Logger LOGGER = LogManager.getLogger();

    public static ZookeeperRegistry zookeeperRegistry;

    private static boolean useRegister = false;

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        Option config = Option.builder("c")
                .argName("file")
                .longOpt("config")
                .hasArg()
                .numberOfArgs(1)
                .required()
                .desc("configuration file")
                .build();

        options.addOption(config);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        String confFilePath = cmd.getOptionValue("c");
        //    String  confFilePath ="/Users/kaideng/work/webank/fate11dev/FATE/arch/networking/proxy/src/main/resources/proxy.properties";
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext-proxy.xml");
        LocalBeanFactory localBeanFactory = context.getBean(LocalBeanFactory.class);
        localBeanFactory.setApplicationContext(context);
        GrpcServerFactory serverFactory = context.getBean(GrpcServerFactory.class);
        Server server = serverFactory.createServer(confFilePath);
        ServerConfManager serverConfManager = context.getBean(ServerConfManager.class);
        ServerConf serverConf = serverConfManager.getServerConf();
        LOGGER.info("Server started listening on port: {}", serverConf.getPort());
        LOGGER.info("server conf: {}", serverConf);
        server.start();
        Properties properties = serverConf.getProperties();
        useRegister = Boolean.valueOf(properties.getProperty("useRegister", "false"));

        if (useRegister) {
            String zkUrl = properties.getProperty("zk.url");
            zookeeperRegistry = ZookeeperRegistry.getRegistery(zkUrl, "proxy", "online", serverConf.getPort());
            zookeeperRegistry.register(FateServer.serviceSets);
            zookeeperRegistry.subProject("serving");
        }

        boolean useJMX = Boolean.valueOf(properties.getProperty("useJMX", "false"));
        if (useJMX) {
            String jmxServerName = properties.getProperty("jmx.server.name", "proxy");
            int jmxPort = Integer.valueOf(System.getProperty("jmx.port", "9999"));
            FateMBeanServer fateMBeanServer = new FateMBeanServer(ManagementFactory.getPlatformMBeanServer(), true);
            String jmxServerUrl = fateMBeanServer.openJMXServer(jmxServerName, jmxPort);
            URL jmxUrl = URL.parseJMXServiceUrl(jmxServerUrl);
            if(useRegister) {
                zookeeperRegistry.register(jmxUrl);
            }
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Use stderr here since the logger may have been reset by its JVM shutdown hook.
            System.err.println("*** shutting down gRPC server since JVM is shutting down");
            if (server != null && useRegister) {
                Set<URL> registered = zookeeperRegistry.getRegistered();
                Set<URL> urls = Sets.newHashSet();
                urls.addAll(registered);
                urls.forEach(url -> {
                    LOGGER.info("unregister {}", url);
                    zookeeperRegistry.unregister(url);
                });
                zookeeperRegistry.destroy();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            server.shutdown();
            System.err.println("*** server shut down");
        }));

        server.awaitTermination();
    }


}
