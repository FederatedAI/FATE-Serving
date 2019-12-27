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

package com.webank.ai.fate.serving.core.bean;

import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NegotiationType;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class GrpcConnectionPool {

    private static final Logger logger = LoggerFactory.getLogger(GrpcConnectionPool.class);
    static private GrpcConnectionPool pool = new GrpcConnectionPool();
    ConcurrentHashMap<String, GenericObjectPool<ManagedChannel>> poolMap = new ConcurrentHashMap<String, GenericObjectPool<ManagedChannel>>();
    private Integer maxTotal = 64;
    private Integer maxIdle = 16;

    private GrpcConnectionPool() {

    }

    static public GrpcConnectionPool getPool() {
        return pool;
    }

    public void returnPool(ManagedChannel channel, String address) {
        try {

            poolMap.get(address).returnObject(channel);

        } catch (Exception e) {
            logger.error("return to pool error", e);
        }
    }

    public ManagedChannel getManagedChannel(String key) throws Exception {

        GenericObjectPool<ManagedChannel> pool = poolMap.get(key);
        if (pool == null) {

            GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();

            poolConfig.setMaxTotal(maxTotal);

            poolConfig.setMinIdle(0);

            poolConfig.setMaxIdle(maxIdle);

            poolConfig.setMaxWaitMillis(-1);

            poolConfig.setLifo(true);

            poolConfig.setMinEvictableIdleTimeMillis(1000L * 60L * 30L);

            poolConfig.setBlockWhenExhausted(true);

            poolConfig.setTestOnBorrow(true);

            String[] ipPort = key.split(":");
            String ip = ipPort[0];
            int port = Integer.parseInt(ipPort[1]);
            poolMap.putIfAbsent(key, new GenericObjectPool<ManagedChannel>
                    (new ManagedChannelFactory(ip, port), poolConfig));

        }

        return poolMap.get(key).borrowObject();
    }

    ;


    private class ManagedChannelFactory extends BasePooledObjectFactory<ManagedChannel> {

        private String ip;
        private int port;

        public ManagedChannelFactory(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }

        @Override
        public ManagedChannel create() throws Exception {


            NettyChannelBuilder builder = NettyChannelBuilder
                    .forAddress(ip, port)
                    .keepAliveTime(6, TimeUnit.MINUTES)
                    .keepAliveTimeout(1, TimeUnit.HOURS)
                    .keepAliveWithoutCalls(true)
                    .idleTimeout(1, TimeUnit.HOURS)
                    .perRpcBufferLimit(128 << 20)
                    .flowControlWindow(32 << 20)
                    .maxInboundMessageSize(32 << 20)
                    .enableRetry()
                    .retryBufferSize(16 << 20)
                    .maxRetryAttempts(20);      // todo: configurable


            builder.negotiationType(NegotiationType.PLAINTEXT)
                    .usePlaintext();

            return builder.build();


        }

        @Override
        public PooledObject<ManagedChannel> wrap(ManagedChannel managedChannel) {
            return new DefaultPooledObject<>(managedChannel);
        }

        @Override
        public void destroyObject(PooledObject<ManagedChannel> p) throws Exception {
            p.getObject().shutdown();
            super.destroyObject(p);
        }

        @Override
        public boolean validateObject(PooledObject<ManagedChannel> channel) {


            return !(channel.getObject().isShutdown() || channel.getObject().isTerminated());
        }


    }
}