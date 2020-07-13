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

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RedisClusterCache extends RedisCache {
    Logger logger = LoggerFactory.getLogger(RedisClusterCache.class);

    String clusterNodes;
    JedisCluster jedisCluster;

    public RedisClusterCache(String clusterNodes) {
        this.clusterNodes = clusterNodes;
    }

    @Override
    public synchronized void init() {
        initClusterConfiguration();
    }

    private void initClusterConfiguration() {
        if (StringUtils.isNotBlank(clusterNodes)) {
            Set<HostAndPort> nodeSet = new HashSet<>();
            try {
                String[] nodes = clusterNodes.split(",");
                if (nodes.length > 0) {
                    for (String node : nodes) {
                        String[] hostAndPost = node.split(":");
                        nodeSet.add(new HostAndPort(hostAndPost[0], Integer.valueOf(hostAndPost[1])));
                    }
                }
            } catch (Exception e) {
                logger.error("redis.cluster.nodes is invalid format");
                e.printStackTrace();
            }

            JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
            jedisPoolConfig.setMaxIdle(maxIdel);
            jedisPoolConfig.setMaxTotal(maxTotal);
            jedisPoolConfig.setMaxWaitMillis(timeout);
            jedisPoolConfig.setTestOnBorrow(true);

            jedisCluster = new JedisCluster(nodeSet, timeout, timeout, 3, password, jedisPoolConfig);
        }
    }

    @Override
    public void put(Object key, Object value) {
        this.put(key, value, expireTime);
    }

    @Override
    public void put(Object key, Object value, int expire) {
        jedisCluster.set(key.toString(), value.toString());
        if (expire > 0) {
            jedisCluster.expire(key.toString(), expire);
        }
    }

    @Override
    public Object get(Object key) {
        return jedisCluster.get(key.toString());
    }

    @Override
    public List get(Object[] keys) {
        return super.get(keys);
    }

    @Override
    public void delete(Object key) {
        jedisCluster.del(key.toString());
    }

    @Override
    public void put(List list) {
        super.put(list);
    }

}
