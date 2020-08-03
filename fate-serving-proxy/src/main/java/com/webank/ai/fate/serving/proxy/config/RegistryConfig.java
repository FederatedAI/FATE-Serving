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

import com.webank.ai.fate.register.router.DefaultRouterService;
import com.webank.ai.fate.register.router.RouterService;
import com.webank.ai.fate.register.zookeeper.ZookeeperRegistry;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RegistryConfig {

    private static final Logger logger = LoggerFactory.getLogger(GrpcConfigration.class);

    @Bean(destroyMethod = "destroy")
    @Conditional(UseZkCondition.class)
    public ZookeeperRegistry zookeeperRegistry() {
        if (logger.isDebugEnabled()) {
            logger.info("prepare to create zookeeper registry ,use zk {}", MetaInfo.PROPERTY_USE_ZK_ROUTER);
        }
        if (StringUtils.isEmpty(MetaInfo.PROPERTY_ZK_URL)) {
            logger.error("useZkRouter is true,but zkUrl is empty,please check zk.url in the config file");
            throw new RuntimeException("wrong zk url");
        }
        ZookeeperRegistry zookeeperRegistry = ZookeeperRegistry.createRegistry(MetaInfo.PROPERTY_ZK_URL, Dict.SERVICE_PROXY,
                Dict.ONLINE_ENVIRONMENT, MetaInfo.PROPERTY_PROXY_GRPC_INTRA_PORT);
        zookeeperRegistry.subProject(Dict.SERVICE_SERVING);

        return zookeeperRegistry;
    }

    @Bean
    @Conditional(UseZkCondition.class)
    @ConditionalOnBean(ZookeeperRegistry.class)
    public ApplicationListener<ApplicationReadyEvent> registerComponent(ZookeeperRegistry zookeeperRegistry) {
        return applicationReadyEvent -> zookeeperRegistry.registerComponent();
    }

    @Bean
    @ConditionalOnBean(ZookeeperRegistry.class)
    public RouterService routerService(ZookeeperRegistry zookeeperRegistry) {
        DefaultRouterService defaultRouterService = new DefaultRouterService();
        defaultRouterService.setRegistry(zookeeperRegistry);
        return defaultRouterService;
    }
}
