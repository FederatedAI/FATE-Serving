package com.webank.ai.fate.serving;


import com.google.common.base.Preconditions;
import com.webank.ai.fate.register.router.DefaultRouterService;
import com.webank.ai.fate.register.router.RouterService;
import com.webank.ai.fate.register.utils.StringUtils;
import com.webank.ai.fate.register.zookeeper.ZookeeperRegistry;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import com.webank.ai.fate.serving.core.bean.SpringContextUtil;
import com.webank.ai.fate.serving.core.cache.Cache;
import com.webank.ai.fate.serving.core.cache.ExpiringLRUCache;
import com.webank.ai.fate.serving.core.cache.RedisCache;
import com.webank.ai.fate.serving.core.flow.FlowCounterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class ServingConfig {

    public static final int VERSION = 200;

    Logger logger = LoggerFactory.getLogger(ServingConfig.class);

    @Bean
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

    @Bean
    @ConditionalOnMissingBean
    public SpringContextUtil springContextUtil() {
        return new SpringContextUtil();
    }

    @Bean
    public FlowCounterManager flowCounterManager() {
        FlowCounterManager flowCounterManager = new FlowCounterManager(Dict.SERVICE_SERVING);
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
