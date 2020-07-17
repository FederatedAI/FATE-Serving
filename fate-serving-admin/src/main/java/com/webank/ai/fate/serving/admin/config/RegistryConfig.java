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
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import com.webank.ai.fate.serving.core.exceptions.SysException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RegistryConfig {

    private static final Logger logger = LoggerFactory.getLogger(RegistryConfig.class);

    @Bean(destroyMethod = "destroy")
    public ZookeeperRegistry zookeeperRegistry() {
        if (logger.isDebugEnabled()) {
            logger.info("prepare to create zookeeper registry ,use zk {}", MetaInfo.PROPERTY_USE_ZK_ROUTER);
        }
        if (MetaInfo.PROPERTY_USE_ZK_ROUTER) {
            if (StringUtils.isEmpty(MetaInfo.PROPERTY_ZK_URL)) {
                logger.error("useZkRouter is true,but zkUrl is empty,please check zk.url in the config file");
                throw new RuntimeException("wrong zk url");
            }

            ZookeeperRegistry zookeeperRegistry = ZookeeperRegistry.createRegistry(MetaInfo.PROPERTY_ZK_URL, Dict.SERVICE_ADMIN, Dict.ONLINE_ENVIRONMENT, MetaInfo.PROPERTY_SERVER_PORT);

            zookeeperRegistry.registerComponent();
            zookeeperRegistry.subProject(Dict.SERVICE_SERVING);
            zookeeperRegistry.subProject(Dict.SERVICE_PROXY);

            if (!zookeeperRegistry.isAvailable()) {
                logger.error("zookeeper registry connection is not available");
                throw new SysException("zookeeper registry connection loss");
            }

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
