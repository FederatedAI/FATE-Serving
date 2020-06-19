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

package com.webank.ai.fate.serving.common.cache;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;

public class ExpiringLRUCache implements Cache {
    private final Map<Object, Object> store;

    public ExpiringLRUCache(int maxSize, int liveTimeSeconds, int intervalSeconds) {
        ExpiringMap<Object, Object> expiringMap = new ExpiringMap<>(maxSize, liveTimeSeconds, intervalSeconds);
        expiringMap.getExpireThread().startExpiryIfNotStarted();
        this.store = expiringMap;
    }

    @Override
    public void put(Object key, Object value) {
        store.put(key, value);
    }

    @Override
    public void put(Object key, Object value, int expire) {
        this.put(key, value);
    }

    @Override
    public Object get(Object key) {
        return store.get(key);
    }

    @Override
    public List<DataWrapper> get(Object[] keys) {
        List<DataWrapper> result = Lists.newArrayList();
        for (Object key : keys) {
            Object singleResult = store.get(key);
            if (singleResult != null) {
                DataWrapper dataWrapper = new DataWrapper(key, singleResult);
                result.add(dataWrapper);
            }
        }
        return result;
    }

    @Override
    public void delete(Object key) {
        store.remove(key);
    }

    @Override
    public void put(List list) {
        for (Object object : list) {
            DataWrapper dataWrapper = (DataWrapper) object;
            this.store.put(dataWrapper.getKey(), dataWrapper.getValue());
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
