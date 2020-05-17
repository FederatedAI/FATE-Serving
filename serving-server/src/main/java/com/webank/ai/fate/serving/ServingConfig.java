package com.webank.ai.fate.serving;


import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jmx.JmxReporter;
import com.google.common.base.Preconditions;
import com.webank.ai.fate.register.router.DefaultRouterService;
import com.webank.ai.fate.register.router.RouterService;
import com.webank.ai.fate.register.utils.StringUtils;
import com.webank.ai.fate.register.zookeeper.ZookeeperRegistry;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.concurrent.TimeUnit;

@Configuration
public class ServingConfig {

    public static final int version = 200;

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
        return ZookeeperRegistry.getRegistery(zkUrl, Dict.PROPERTY_SERVING_ADDRESS, Dict.SELF_ENVIRONMENT, port);
    }

    @Bean
    public MetricRegistry metrics() {
        return new MetricRegistry();
    }

    @Bean
    public Meter requestMeter(MetricRegistry metrics) {
        return metrics.meter("request");
    }

    @Bean
    public Counter pendingJobs(MetricRegistry metrics) {
        return metrics.counter("requestCount");
    }

    @Bean
    public ConsoleReporter consoleReporter(MetricRegistry metrics) {
        return ConsoleReporter.forRegistry(metrics)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
    }

    @Bean
    public JmxReporter jmxReporter(MetricRegistry metrics) {
        return JmxReporter.forRegistry(metrics).build();
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
    public FlowCounterManager FlowCounterManager(){
        FlowCounterManager flowCounterManager = new  FlowCounterManager();
       // flowCounterManager.setMetricReport(new ());
        flowCounterManager.startReport();
        return  flowCounterManager;

    }


    @Bean
    public Cache cache() {

        String cacheType = environment.getProperty("cache.type", "local");
        logger.info("cache type is {},prepare to build cache", cacheType);
        Cache cache = null;
        switch (cacheType) {
            case "redis":
                RedisCache redisCache = new RedisCache();
                String ip = environment.getProperty("redis.ip");
                String password = environment.getProperty("redis.password");
                Integer port = environment.getProperty("redis.port", Integer.class);
                Integer timeout = environment.getProperty("redis.timeout", Integer.class, 2000);
                Integer maxTotal = environment.getProperty("redis.maxTotal", Integer.class, 20);
                Integer maxIdle = environment.getProperty("redis.maxIdle", Integer.class, 20);
                Integer expire = environment.getProperty("redis.expire", Integer.class);
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
                Integer maxSize = environment.getProperty("local.cache.maxsize", Integer.class, 10000);
                Integer expireTime = environment.getProperty("local.cache.expire", Integer.class, 30);
                Integer interval = environment.getProperty("local.cache.interval", Integer.class, 3);
                ExpiringLRUCache lruCache = new ExpiringLRUCache(maxSize, expireTime, interval);
                cache = lruCache;
                break;
            default:

        }

        return cache;
    }


}
