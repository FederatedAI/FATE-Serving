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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ExpiringMap<K, V> extends LinkedHashMap<K, V> {

    private static final int DEFAULT_TIME_TO_LIVE = 180;
    private static final int DEFAULT_MAX_CAPACITY = 1000;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private static final int DEFAULT_EXPIRATION_INTERVAL = 1;
    private static AtomicInteger expireCount = new AtomicInteger(1);
    private final Lock lock = new ReentrantLock();
    private final ConcurrentHashMap<K, ExpiryObject> delegateMap;
    private final ExpireThread expireThread;
    private volatile int maxCapacity;

    public ExpiringMap() {
        this(DEFAULT_MAX_CAPACITY, DEFAULT_TIME_TO_LIVE, DEFAULT_EXPIRATION_INTERVAL);
    }

    public ExpiringMap(int maxCapacity, int timeToLive, int expirationInterval) {
        this.maxCapacity = maxCapacity;
        this.delegateMap = new ConcurrentHashMap<>();
        this.expireThread = new ExpireThread();
        expireThread.setTimeToLive(timeToLive);
        expireThread.setExpirationInterval(expirationInterval);
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxCapacity;
    }

    @Override
    public V put(K key, V value) {
        ExpiryObject answer = delegateMap.put(key, new ExpiryObject(key, value, System.currentTimeMillis()));
        if (answer == null) {
            return null;
        }
        return answer.getValue();
    }

    @Override
    public V get(Object key) {
        ExpiryObject object = delegateMap.get(key);
        if (object != null) {
            object.setLastAccessTime(System.currentTimeMillis());
            return object.getValue();
        }
        return null;
    }

    @Override
    public V remove(Object key) {
        ExpiryObject answer = delegateMap.remove(key);
        if (answer == null) {
            return null;
        }
        return answer.getValue();
    }

    @Override
    public boolean containsKey(Object key) {
        return delegateMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return delegateMap.containsValue(value);
    }

    @Override
    public int size() {
        return delegateMap.size();
    }

    @Override
    public boolean isEmpty() {
        return delegateMap.isEmpty();
    }

    @Override
    public void clear() {
        delegateMap.clear();
        expireThread.stopExpiring();
    }

    @Override
    public int hashCode() {
        return delegateMap.hashCode();
    }

    @Override
    public Set<K> keySet() {
        return delegateMap.keySet();
    }

    @Override
    public boolean equals(Object obj) {
        return delegateMap.equals(obj);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> inMap) {
        throw new UnsupportedOperationException();
    }

//    @Override
//    public Collection<V> values() {
//        List<V> list = new ArrayList<V>();
//        Set<Entry<K, ExpiryObject>> delegatedSet = delegateMap.entrySet();
//        for (Entry<K, ExpiryObject> entry : delegatedSet) {
//            ExpiryObject value = entry.getValue();
//            list.add(value.getValue());
//        }
//        return list;
//    }

//    @Override
//    public Set<Entry<K, V>> entrySet() {
//        throw new UnsupportedOperationException();
//    }

    public ExpireThread getExpireThread() {
        return expireThread;
    }

    public int getExpirationInterval() {
        return expireThread.getExpirationInterval();
    }

    public void setExpirationInterval(int expirationInterval) {
        expireThread.setExpirationInterval(expirationInterval);
    }

    public int getTimeToLive() {
        return expireThread.getTimeToLive();
    }

    public void setTimeToLive(int timeToLive) {
        expireThread.setTimeToLive(timeToLive);
    }

    @Override
    public String toString() {
        return "ExpiringMap{" +
                "delegateMap=" + delegateMap.toString() +
                ", expireThread=" + expireThread.toString() +
                '}';
    }

    /**
     * can be expired object
     */
    private class ExpiryObject {
        private K key;
        private V value;
        private AtomicLong lastAccessTime;

        ExpiryObject(K key, V value, long lastAccessTime) {
            if (value == null) {
                throw new IllegalArgumentException("An expiring object cannot be null.");
            }
            this.key = key;
            this.value = value;
            this.lastAccessTime = new AtomicLong(lastAccessTime);
        }

        public long getLastAccessTime() {
            return lastAccessTime.get();
        }

        public void setLastAccessTime(long lastAccessTime) {
            this.lastAccessTime.set(lastAccessTime);
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        @Override
        public boolean equals(Object obj) {
            return value.equals(obj);
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }

        @Override
        public String toString() {
            return "ExpiryObject{" +
                    "key=" + key +
                    ", value=" + value +
                    ", lastAccessTime=" + lastAccessTime +
                    '}';
        }
    }

    /**
     * Background thread, periodically checking if the data is out of date
     */
    public class ExpireThread implements Runnable {
        private final Thread expirerThread;
        private long timeToLiveMillis;
        private long expirationIntervalMillis;
        private volatile boolean running = false;

        public ExpireThread() {
            expirerThread = new Thread(this, "ExpiryMapExpire-" + expireCount.getAndIncrement());
            expirerThread.setDaemon(true);
        }

        @Override
        public String toString() {
            return "ExpireThread{" +
                    ", timeToLiveMillis=" + timeToLiveMillis +
                    ", expirationIntervalMillis=" + expirationIntervalMillis +
                    ", running=" + running +
                    ", expirerThread=" + expirerThread +
                    '}';
        }

        @Override
        public void run() {
            while (running) {
                processExpires();
                try {
                    Thread.sleep(expirationIntervalMillis);
                } catch (InterruptedException e) {
                    running = false;
                }
            }
        }

        private void processExpires() {
            long timeNow = System.currentTimeMillis();
            for (ExpiryObject o : delegateMap.values()) {
                if (timeToLiveMillis <= 0) {
                    continue;
                }
                long timeIdle = timeNow - o.getLastAccessTime();
                if (timeIdle >= timeToLiveMillis) {
                    delegateMap.remove(o.getKey());
                }
            }
        }

        /**
         * start expiring Thread
         */
        public void startExpiring() {
            if (!running) {
                running = true;
                expirerThread.start();
            }
        }

        /**
         * start thread
         */
        public void startExpiryIfNotStarted() {
            if (running) {
                return;
            }
            startExpiring();
        }

        /**
         * stop thread
         */
        public void stopExpiring() {
            if (running) {
                running = false;
                expirerThread.interrupt();
            }
        }

        /**
         * get thread state
         *
         * @return thread state
         */
        public boolean isRunning() {
            return running;
        }

        /**
         * get time to live
         *
         * @return time to live
         */
        public int getTimeToLive() {
            return (int) timeToLiveMillis / 1000;
        }

        /**
         * update time to live
         *
         * @param timeToLive time to live
         */
        public void setTimeToLive(long timeToLive) {
            this.timeToLiveMillis = timeToLive * 1000;
        }

        /**
         * get expiration interval
         *
         * @return expiration interval (second)
         */
        public int getExpirationInterval() {
            return (int) expirationIntervalMillis / 1000;
        }

        /**
         * set expiration interval
         *
         * @param expirationInterval expiration interval (second)
         */
        public void setExpirationInterval(long expirationInterval) {
            this.expirationIntervalMillis = expirationInterval * 1000;
        }
    }
}