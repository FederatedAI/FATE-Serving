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

import com.google.common.base.Preconditions;
import com.webank.ai.fate.register.router.DefaultRouterService;
import com.webank.ai.fate.register.router.RouterService;
import com.webank.ai.fate.register.utils.StringUtils;
import com.webank.ai.fate.register.zookeeper.ZookeeperRegistry;
import com.webank.ai.fate.serving.common.cache.Cache;
import com.webank.ai.fate.serving.common.cache.ExpiringLRUCache;
import com.webank.ai.fate.serving.common.cache.RedisCache;
import com.webank.ai.fate.serving.common.flow.FlowCounterManager;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServingConfig {

    public static final int VERSION = 200;

    Logger logger = LoggerFactory.getLogger(ServingConfig.class);

    @Bean(destroyMethod = "destroy")
    @Conditional({UseZkCondition.class})
    ZookeeperRegistry getServiceRegistry() {
        Preconditions.checkArgument(StringUtils.isNotEmpty(MetaInfo.PROPERTY_ZK_URL));
        return ZookeeperRegistry.getRegistry(MetaInfo.PROPERTY_ZK_URL, Dict.PROPERTY_SERVING_ADDRESS, Dict.ONLINE_ENVIRONMENT, MetaInfo.PROPERTY_PORT);
    }

    @Bean
    @Conditional({UseZkCondition.class})
    RouterService getRouterService(ZookeeperRegistry zookeeperRegistry) {
        DefaultRouterService routerService = new DefaultRouterService();
        routerService.setRegistry(zookeeperRegistry);
        return routerService;
    }

    @Bean(destroyMethod = "destroy")
    public FlowCounterManager flowCounterManager() {
        FlowCounterManager flowCounterManager = new FlowCounterManager(Dict.SERVICE_SERVING, true);
        flowCounterManager.startReport();
        return flowCounterManager;
    }

    @Bean
    public Cache cache() {
        String cacheType = MetaInfo.PROPERTY_CACHE_TYPE;
        logger.info("cache type is {},prepare to build cache", cacheType);
        Cache cache = null;
        switch (cacheType) {
            case "redis":
                RedisCache redisCache = new RedisCache();
                String ip = MetaInfo.PROPERTY_REDIS_IP;
                String password = MetaInfo.PROPERTY_REDIS_PASSWORD;
                Integer port = MetaInfo.PROPERTY_REDIS_PORT;
                Integer timeout = MetaInfo.PROPERTY_REDIS_TIMEOUT;
                Integer maxTotal = MetaInfo.PROPERTY_REDIS_MAX_TOTAL;
                Integer maxIdle = MetaInfo.PROPERTY_REDIS_MAX_IDLE;
                Integer expire = MetaInfo.PROPERTY_REDIS_EXPIRE;
                redisCache.setExpireTime(timeout);
                redisCache.setMaxTotal(maxTotal);
                redisCache.setMaxIdel(maxIdle);
                redisCache.setHost(ip);
                redisCache.setPort(port);
                redisCache.setExpireTime(expire != null ? expire : -1);
                redisCache.setPassword(password);
                redisCache.init();
                cache = redisCache;
                break;
            case "local":
                Integer maxSize = MetaInfo.PROPERTY_LOCAL_CACHE_MAXSIZE;
                Integer expireTime = MetaInfo.PROPERTY_LOCAL_CACHE_EXPIRE;
                Integer interval = MetaInfo.PROPERTY_LOCAL_CACHE_INTERVAL;
                ExpiringLRUCache lruCache = new ExpiringLRUCache(maxSize, expireTime, interval);
                cache = lruCache;
                break;
            default:
        }

        return cache;
    }

}
