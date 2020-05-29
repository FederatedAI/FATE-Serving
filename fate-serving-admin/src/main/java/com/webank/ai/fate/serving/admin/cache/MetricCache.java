//package com.webank.ai.fate.serving.admin.cache;
//
//import com.google.common.cache.CacheBuilder;
//import com.webank.ai.fate.serving.core.bean.MetricEntity;
//import com.webank.ai.fate.serving.core.cache.Cache;
//import org.springframework.beans.factory.InitializingBean;
//import org.springframework.stereotype.Component;
//
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//import java.util.concurrent.TimeUnit;
//
//@Component
//public class MetricCache implements Cache<String, MetricEntity>, InitializingBean {
//
//    private com.google.common.cache.Cache<String, MetricEntity> cache;
//
//    private Set<String> resourceSet = new HashSet<>();
//
//    public Set<String> getResourceSet() {
//        return resourceSet;
//    }
//
//    @Override
//    public void put(String key, MetricEntity value) {
//        this.cache.put(key, value);
//    }
//
//    @Override
//    public MetricEntity get(String key) {
//        return this.cache.getIfPresent(key);
//    }
//
//    @Override
//    public List<DataWrapper> get(String... keys) {
//        return null;
//    }
//
//    @Override
//    public void put(List<DataWrapper> dataWrappers) {
//
//    }
//
//    public void addResource(String resource) {
//        if (resource != null && resource.length() > 0) {
//            this.resourceSet.add(resource);
//        }
//    }
//
//    @Override
//    public void afterPropertiesSet() throws Exception {
//        this.cache = CacheBuilder.newBuilder().expireAfterWrite(60, TimeUnit.SECONDS).build();
//    }
//}
