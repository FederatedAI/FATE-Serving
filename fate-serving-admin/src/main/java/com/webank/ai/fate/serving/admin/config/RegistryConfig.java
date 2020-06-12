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

package com.webank.ai.fate.serving.admin.config;

import com.webank.ai.fate.register.zookeeper.ZookeeperRegistry;
import com.webank.ai.fate.serving.core.bean.Dict;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class RegistryConfig {

    private static final Logger logger = LoggerFactory.getLogger(RegistryConfig.class);

    @Value("${server.port:8350}")
    private Integer port;

    @Value("${zk.url:}")
    private String zkUrl;

    @Value("${useZkRouter:true}")
    private boolean useZkRouter;

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
        if (useZkRouter) {
            if (StringUtils.isEmpty(zkUrl)) {
                logger.error("useZkRouter is true,but zkUrl is empty,please check zk.url in the config file");
                throw new RuntimeException("wrong zk url");
            }

            System.setProperty("acl.enable", Optional.ofNullable(aclEnable).orElse(""));
            System.setProperty("acl.username", Optional.ofNullable(aclUsername).orElse(""));
            System.setProperty("acl.password", Optional.ofNullable(aclPassword).orElse(""));

            ZookeeperRegistry zookeeperRegistry = ZookeeperRegistry.getRegistry(zkUrl, Dict.SERVICE_ADMIN, Dict.ONLINE_ENVIRONMENT, port);
            zookeeperRegistry.subProject(Dict.SERVICE_SERVING);
            zookeeperRegistry.subProject(Dict.SERVICE_PROXY);

            zookeeperRegistry.registerComponent();

            return zookeeperRegistry;
        }
        return null;
    }

    /*@Bean
    @ConditionalOnBean(ZookeeperRegistry.class)
    public RouterService routerService(ZookeeperRegistry zookeeperRegistry) {
        DefaultRouterService defaultRouterService = new DefaultRouterService();
        defaultRouterService.setRegistry(zookeeperRegistry);
        return defaultRouterService;
    }*/
}
