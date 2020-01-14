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
import com.webank.ai.fate.serving.proxy.common.Dict;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class RegistryConfig {

    @Value("${proxy.grpc.intra.port:8867}")
    private Integer port;

    @Value("${zk.url:zookeeper://localhost:2181}")
    private String zkUrl;

    @Value("${useZkRouter:false}")
    private String useZkRouter;

    @Value("${acl.enable}")
    private String aclEnable;

    @Value("${acl.username}")
    private String aclUsername;

    @Value("${acl.password}")
    private String aclPassword;

    @Bean
    public ZookeeperRegistry zookeeperRegistry() {
        if ("true".equals(useZkRouter) && StringUtils.isNotEmpty(zkUrl)) {
            System.setProperty("acl.enable", Optional.ofNullable(aclEnable).orElse(""));
            System.setProperty("acl.username", Optional.ofNullable(aclUsername).orElse(""));
            System.setProperty("acl.password", Optional.ofNullable(aclPassword).orElse(""));

            ZookeeperRegistry zookeeperRegistry = ZookeeperRegistry.getRegistery(zkUrl, Dict.SELF_PROJECT_NAME,
                    Dict.SELF_ENVIRONMENT, Integer.valueOf(port));
            zookeeperRegistry.register(FateServer.serviceSets);
            zookeeperRegistry.subProject("serving");
            return zookeeperRegistry;
        }
        return null;
    }

    @Bean
    public RouterService routerService(@Autowired(required=false) ZookeeperRegistry zookeeperRegistry) {
        if (zookeeperRegistry != null) {
            DefaultRouterService defaultRouterService = new DefaultRouterService();
            defaultRouterService.setRegistry(zookeeperRegistry);
            return defaultRouterService;
        }
        return null;
    }
}
