package com.webank.ai.fate.serving.admin.config;


import com.webank.ai.fate.serving.admin.interceptors.LoginInterceptor;
import com.webank.ai.fate.serving.common.cache.Cache;
import com.webank.ai.fate.serving.common.cache.ExpiringLRUCache;
import com.webank.ai.fate.serving.common.cache.RedisCache;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
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
                .excludePathPatterns("/api/admin/login", "/api/component/list", "/api/monitor/queryJvm", "/api/monitor/query", "/api/monitor/queryModel");

//        registry.addInterceptor(requestInterceptor()).addPathPatterns("/api/model/**");
    }

//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("swagger-ui.html")
//                .addResourceLocations("classpath:/META-INF/resources/");
//        registry.addResourceHandler("/webjars/**")
//                .addResourceLocations("classpath:/META-INF/resources/webjars/");
//    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(86400);
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
