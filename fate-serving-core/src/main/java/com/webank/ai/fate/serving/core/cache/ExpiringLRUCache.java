
package com.webank.ai.fate.serving.core.cache;


import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;


public class ExpiringLRUCache implements Cache {
    private final Map<Object, Object> store;

    public ExpiringLRUCache(int maxSize,int  liveTimeSeconds,int  intervalSeconds) {
        ExpiringMap<Object, Object> expiringMap = new ExpiringMap<>(maxSize,liveTimeSeconds, intervalSeconds);
        expiringMap.getExpireThread().startExpiryIfNotStarted();
        this.store = expiringMap;
    }

    @Override
    public void put(Object key, Object value) {
        store.put(key, value);
    }

    @Override
    public Object get(Object key) {
        return store.get(key);
    }

    @Override
    public List<DataWrapper> get(Object[] keys) {
        List<DataWrapper>  result = Lists.newArrayList();
        for(Object  key:keys) {
           Object singleResult =  store.get(key);
           if(singleResult!=null) {
               DataWrapper  dataWrapper = new DataWrapper(key,singleResult);
               result.add(dataWrapper);
           }
        }
        return  result;
    }

    @Override
    public void put(List list) {
        for(Object object: list ) {
            DataWrapper  dataWrapper  =  (DataWrapper) object;
            this.store.put(dataWrapper.getKey(),dataWrapper.getValue());
        }
    }

//    @Override
//    public void put(List<DataWrapper> dataWrappers) {
//        for(DataWrapper dataWrapper: dataWrappers ) {
//            this.store.put(dataWrapper.getKey(),dataWrapper.getValue());
//        }
//
//    }


}
