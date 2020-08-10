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

import com.webank.ai.fate.serving.admin.interceptors.LoginInterceptor;
import com.webank.ai.fate.serving.common.cache.Cache;
import com.webank.ai.fate.serving.common.cache.ExpiringLRUCache;
import com.webank.ai.fate.serving.common.cache.RedisCache;
import com.webank.ai.fate.serving.common.cache.RedisClusterCache;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableScheduling
public class WebConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

    @Bean
    public LoginInterceptor loginInterceptor() {
        return new LoginInterceptor();
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/admin/login");
    }

    /*@Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }*/

    /*@Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(86400);
    }*/

    @Bean
    public Cache cache() {
        String cacheType = MetaInfo.PROPERTY_CACHE_TYPE;
        logger.info("cache type is {},prepare to build cache", cacheType);
        Cache cache = null;
        switch (cacheType) {
            case "redis":
                String ip = MetaInfo.PROPERTY_REDIS_IP;
                String password = MetaInfo.PROPERTY_REDIS_PASSWORD;
                Integer port = MetaInfo.PROPERTY_REDIS_PORT;
                Integer timeout = MetaInfo.PROPERTY_REDIS_TIMEOUT;
                Integer maxTotal = MetaInfo.PROPERTY_REDIS_MAX_TOTAL;
                Integer maxIdle = MetaInfo.PROPERTY_REDIS_MAX_IDLE;
                Integer expire = MetaInfo.PROPERTY_REDIS_EXPIRE;
                String clusterNodes = MetaInfo.PROPERTY_REDIS_CLUSTER_NODES;

                RedisCache redisCache;
                if (StringUtils.isNotBlank(clusterNodes)) {
                    redisCache = new RedisClusterCache(clusterNodes);
                    logger.info("redis cache mode: cluster");
                } else {
                    redisCache = new RedisCache();
                    logger.info("redis cache mode: standalone");
                }
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
