
package com.webank.ai.fate.serving.core.cache;


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

}
