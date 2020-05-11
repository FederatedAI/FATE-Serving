package com.webank.ai.fate.serving.core.cache;

import com.google.common.collect.Lists;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Pipeline;

import java.util.List;

public class RedisCache implements Cache {
    int expireTime;
    String host;
    int port;
    int timeout;
    String password;
    int maxTotal;
    int maxIdel;
    JedisPool jedisPool;

    synchronized public void init() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(maxTotal);
        jedisPoolConfig.setMaxIdle(maxIdel);
//        this.jedisPool = new JedisPool(jedisPoolConfig, host, port, timeout, null);
        this.jedisPool = new JedisPool(jedisPoolConfig, host, port, timeout, password);
    }

    @Override
    public void put(Object key, Object value) {
        try (Jedis jedis = jedisPool.getResource()) {
            Pipeline redisPipeline = jedis.pipelined();
            redisPipeline.set(key.toString(), value.toString());
            if (expireTime > 0) {
                redisPipeline.expire(key.toString(), expireTime);
            }
            redisPipeline.sync();
        }
    }

    @Override
    public Object get(Object key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key.toString());
        }
    }

    @Override
    public List get(Object[] keys) {
        List<DataWrapper> result = Lists.newArrayList();
        for (Object key : keys) {
            Object singleResult = this.get(key);
            if (singleResult != null) {
                DataWrapper dataWrapper = new DataWrapper(key, singleResult);
                result.add(dataWrapper);
            }
        }
        return result;
    }

    @Override
    public void put(List list) {
        for (Object object : list) {
            DataWrapper dataWrapper = (DataWrapper) object;
            this.put(dataWrapper.getKey(), dataWrapper.getValue());
        }
    }


    public int getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(int expireTime) {
        this.expireTime = expireTime;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getMaxTotal() {
        return maxTotal;
    }

    public void setMaxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
    }

    public int getMaxIdel() {
        return maxIdel;
    }

    public void setMaxIdel(int maxIdel) {
        this.maxIdel = maxIdel;
    }
}
