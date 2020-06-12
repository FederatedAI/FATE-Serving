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

package com.webank.ai.fate.serving.proxy.config;

import com.webank.ai.fate.register.provider.FateServer;
import com.webank.ai.fate.register.router.DefaultRouterService;
import com.webank.ai.fate.register.router.RouterService;
import com.webank.ai.fate.register.zookeeper.ZookeeperRegistry;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.proxy.rpc.grpc.InterGrpcServer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class RegistryConfig {

    private static final Logger logger = LoggerFactory.getLogger(GrpcConfigration.class);

    @Value("${proxy.grpc.intra.port:8867}")
    private Integer port;

    @Value("${zk.url:}")
    private String zkUrl;

    @Value("${useZkRouter:true}")
    private String useZkRouter;

    @Value("${acl.enable:false}")
    private String aclEnable;

    @Value("${acl.username:}")
    private String aclUsername;

    @Value("${acl.password:}")
    private String aclPassword;

    @Bean(destroyMethod = "destroy")
    public ZookeeperRegistry zookeeperRegistry() {
        if (logger.isDebugEnabled()) {
            logger.info("prepare to create zookeeper registry ,use zk {}", useZkRouter);
        }
        if ("true".equals(useZkRouter)) {

            if (StringUtils.isEmpty(zkUrl)) {
                logger.error("useZkRouter is true,but zkUrl is empty,please check zk.url in the config file");
                throw new RuntimeException("wrong zk url");
            }
            System.setProperty("acl.enable", Optional.ofNullable(aclEnable).orElse(""));
            System.setProperty("acl.username", Optional.ofNullable(aclUsername).orElse(""));
            System.setProperty("acl.password", Optional.ofNullable(aclPassword).orElse(""));
            ZookeeperRegistry zookeeperRegistry = ZookeeperRegistry.getRegistry(zkUrl, Dict.SERVICE_PROXY,
                    Dict.ONLINE_ENVIRONMENT, Integer.valueOf(port));
            logger.info("register zk , {}", FateServer.serviceSets);
            zookeeperRegistry.register(FateServer.serviceSets);
            zookeeperRegistry.subProject(Dict.SERVICE_SERVING);

            zookeeperRegistry.registerComponent();

            return zookeeperRegistry;
        }
        return null;
    }

    @Bean
    @ConditionalOnBean(ZookeeperRegistry.class)
    public RouterService routerService(ZookeeperRegistry zookeeperRegistry) {
        if (zookeeperRegistry != null) {
            DefaultRouterService defaultRouterService = new DefaultRouterService();
            defaultRouterService.setRegistry(zookeeperRegistry);
            return defaultRouterService;
        }
        return null;
    }
}
