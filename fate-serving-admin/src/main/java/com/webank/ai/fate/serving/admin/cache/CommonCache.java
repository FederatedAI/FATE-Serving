package com.webank.ai.fate.serving.admin.cache;

public interface CommonCache<K, V> {

    void put(K key, V value);

    V get(K key);
}
