
package com.webank.ai.fate.serving.core.cache;

import java.util.List;

public interface Cache<K, V> {

    public  static  class DataWrapper<K, V>{
        public K getKey() {
            return key;
        }

        public void setKey(K key) {
            this.key = key;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }

        public DataWrapper(K key, V value) {
            this.key = key;
            this.value = value;
        }

        K  key;
        V  value;
    }

    void put(K key, V value);

    V get(K key);

    List<DataWrapper> get(K... keys);

    void put(List<DataWrapper> dataWrappers);

    

}
