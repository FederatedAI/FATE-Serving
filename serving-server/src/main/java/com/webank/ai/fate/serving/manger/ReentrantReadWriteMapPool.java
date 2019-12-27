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

package com.webank.ai.fate.serving.manger;


import com.webank.ai.fate.serving.core.bean.BaseMapPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class ReentrantReadWriteMapPool<K, V> extends BaseMapPool<K, V> {
    private static final Logger logger = LoggerFactory.getLogger(ReentrantReadWriteMapPool.class);
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();
    private Map<K, V> pool;

    public ReentrantReadWriteMapPool(Map<K, V> staticMap) {
        this.pool = staticMap;
    }


    public Map getDataMap() {
        return pool;
    }


    @Override
    public void put(K key, V value) {
        this.writeLock.lock();
        try {
            pool.put(key, value);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        } finally {
            this.writeLock.unlock();
        }
    }

    @Override
    public void putIfAbsent(K key, V value) {
        this.writeLock.lock();
        try {
            pool.putIfAbsent(key, value);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        } finally {
            this.writeLock.unlock();
        }
    }

    @Override
    public void putAll(Map<K, V> kv) {
        this.writeLock.lock();
        try {
            pool.putAll(kv);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        } finally {
            this.writeLock.unlock();
        }
    }

    @Override
    public V get(K key) {
        this.readLock.lock();
        V value = pool.get(key);
        this.readLock.unlock();
        return value;
    }

    public ArrayList<K> keys() {
        if (pool.size() > 0) {
            return new ArrayList<K>(pool.keySet());
        } else {
            return null;
        }
    }
}
