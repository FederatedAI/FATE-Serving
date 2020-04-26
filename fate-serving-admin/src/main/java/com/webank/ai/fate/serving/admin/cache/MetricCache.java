package com.webank.ai.fate.serving.admin.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.webank.ai.fate.serving.admin.bean.MetricEntity;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class MetricCache implements CommonCache<String, MetricEntity>, InitializingBean {

    private Cache<String, MetricEntity> cache;

    private Set<String> resourceSet = new HashSet<>();

    public Set<String> getResourceSet() {
        return resourceSet;
    }

    @Override
    public void put(String key, MetricEntity value) {
        this.cache.put(key, value);
    }

    @Override
    public MetricEntity get(String key) {
        return this.cache.getIfPresent(key);
    }

    public void addResource(String resource) {
        if (resource != null && resource.length() > 0) {
            this.resourceSet.add(resource);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.cache = CacheBuilder.newBuilder().expireAfterWrite(60, TimeUnit.SECONDS).build();
    }
}
