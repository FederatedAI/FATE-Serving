package com.webank.ai.fate.serving;


import com.google.common.base.Preconditions;
import com.webank.ai.fate.register.router.DefaultRouterService;
import com.webank.ai.fate.register.router.RouterService;
import com.webank.ai.fate.register.utils.StringUtils;
import com.webank.ai.fate.register.zookeeper.ZookeeperRegistry;
import com.webank.ai.fate.serving.core.async.AbstractAsyncMessageProcessor;
import com.webank.ai.fate.serving.core.async.AsyncSubscribeRegister;
import com.webank.ai.fate.serving.core.async.Subscribe;
import com.webank.ai.fate.serving.core.bean.Dict;
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
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class ServingConfig {

    public static final int VERSION = 200;

    Logger logger = LoggerFactory.getLogger(ServingConfig.class);

    @Autowired
    Environment environment;

    @Value("${port:3000}")
    int port;

    @Bean
    @Conditional({UseZkCondition.class})
    ZookeeperRegistry getServiceRegistry() {
        String zkUrl = environment.getProperty("zk.url");
        Preconditions.checkArgument(StringUtils.isNotEmpty(zkUrl));
        return ZookeeperRegistry.getRegistry(zkUrl, Dict.PROPERTY_SERVING_ADDRESS, Dict.ONLINE_ENVIRONMENT, port);
    }

    @Bean
    @Conditional({UseZkCondition.class})
    RouterService getRouterService(ZookeeperRegistry zookeeperRegistry) {
        DefaultRouterService routerService = new DefaultRouterService();
        routerService.setRegistry(zookeeperRegistry);
        return routerService;
    }


//    @Bean
//    public ApplicationListener<ApplicationEnvironmentPreparedEvent> prepareMeta() {
//
//        return new ApplicationListener<ApplicationEnvironmentPreparedEvent>() {
//            @Override
//            public void onApplicationEvent(ApplicationEnvironmentPreparedEvent applicationReadyEvent) {
//
//                ConfigurableEnvironment  environment =  applicationReadyEvent.getEnvironment();
//
//                int processors = Runtime.getRuntime().availableProcessors();
//       //  String    P               address = environment.getProperty(Dict.PROPERTY_PROXY_ADDRESS);
//         MetaInfo.PROPERTY_PROXY_ADDRESS =environment.getProperty(Dict.PROPERTY_PROXY_ADDRESS);
//         MetaInfo.SERVING_CORE_POOL_SIZE = environment.getProperty(Dict.SERVING_CORE_POOL_SIZE, int.class, processors);
//         MetaInfo.SERVING_MAX_POOL_SIZE =environment.getProperty(Dict.SERVING_MAX_POOL_SIZE, int.class, processors * 2);
//         MetaInfo.SERVING_POOL_ALIVE_TIME= environment.getProperty(Dict.SERVING_POOL_ALIVE_TIME, int.class, 1000);
//         MetaInfo.USE_REGISTER =   environment.getProperty(Dict.USE_REGISTER, boolean.class, Boolean.TRUE);
//         MetaInfo.FEATURE_BATCH_ADAPTOR = environment.getProperty(Dict.FEATURE_BATCH_ADAPTOR);
//
////         boolean useRegister = environment.getProperty(Dict.USE_REGISTER, boolean.class, Boolean.TRUE);
////
////    String ip = environment.getProperty("redis.ip");
////    String password = environment.getProperty("redis.password");
////    Integer port = environment.getProperty("redis.port", Integer.class);
////    Integer timeout = environment.getProperty("redis.timeout", Integer.class, 2000);
////    Integer maxTotal = environment.getProperty("redis.maxTotal", Integer.class, 20);
////    Integer maxIdle = environment.getProperty("redis.maxIdle", Integer.class, 20);
////    Integer expire = environment.getProperty("redis.expire", Integer.class);
////
////    Integer maxSize = environment.getProperty("local.cache.maxsize", Integer.class, 10000);
////    Integer expireTime = environment.getProperty("local.cache.expire", Integer.class, 30);
////    Integer interval = environment.getProperty("local.cache.interval", Integer.class, 3);
//
//
//
//                }
//        };
//    }




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
        String cacheType = environment.getProperty("cache.type", "local");
        logger.info("cache type is {},prepare to build cache", cacheType);
        Cache cache = null;
        switch (cacheType) {
            case "redis":
                RedisCache redisCache = new RedisCache();
                String ip = environment.getProperty(Dict.PROPERTY_REDIS_IP);
                String password = environment.getProperty(Dict.PROPERTY_REDIS_PASSWORD);
                Integer port = environment.getProperty(Dict.PROPERTY_REDIS_PORT, Integer.class);
                Integer timeout = environment.getProperty(Dict.PROPERTY_REDIS_TIMEOUT, Integer.class, 2000);
                Integer maxTotal = environment.getProperty(Dict.PROPERTY_REDIS_MAX_TOTAL, Integer.class, 20);
                Integer maxIdle = environment.getProperty(Dict.PROPERTY_REDIS_MAX_IDLE, Integer.class, 20);
                Integer expire = environment.getProperty(Dict.PROPERTY_REDIS_EXPIRE, Integer.class);
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
                Integer maxSize = environment.getProperty(Dict.PROPERTY_LOCAL_CACHE_MAXSIZE, Integer.class, 10000);
                Integer expireTime = environment.getProperty(Dict.PROPERTY_LOCAL_CACHE_EXPIRE, Integer.class, 30);
                Integer interval = environment.getProperty(Dict.PROPERTY_LOCAL_CACHE_INTERVAL, Integer.class, 3);
                ExpiringLRUCache lruCache = new ExpiringLRUCache(maxSize, expireTime, interval);
                cache = lruCache;
                break;
            default:
        }

        return cache;
    }


}
