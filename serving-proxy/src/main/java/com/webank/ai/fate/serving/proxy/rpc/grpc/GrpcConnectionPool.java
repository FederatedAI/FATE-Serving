package com.webank.ai.fate.serving.proxy.rpc.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultEvictionPolicy;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


/**
 * @Description grpc 连接池
 * @Author
 **/
@Service
public class GrpcConnectionPool {

    Logger logger = LoggerFactory.getLogger(GrpcConnectionPool.class);

    ConcurrentHashMap<String, GenericObjectPool<ManagedChannel>> poolMap = new ConcurrentHashMap<String, GenericObjectPool<ManagedChannel>>();

    public void returnPool(ManagedChannel channel, String host, int port) {
        try {
            logger.info("return grpc pool {}:{}",host,port);
            String key = host + ":" + port;
            poolMap.get(key).returnObject(channel);
            // logger.info("pool active size {}",poolMap.get(key).());
        } catch (Exception e) {
            logger.error("return to pool error", e);
        }

    }

    @Value("${proxy.grpc.pool.maxTotal:64}")
    private Integer maxTotal;
    @Value("${proxy.grpc.pool.maxIdle:1}")
    private Integer maxIdle;

    public ManagedChannel getManagedChannel(String host, int port) throws Exception {
        String key = host + ":" + port;
        logger.info("try to get grpc channel {}",key);
        GenericObjectPool<ManagedChannel> pool = poolMap.get(key);
        if (pool == null) {

            GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();

            poolConfig.setMaxTotal(maxTotal);

            poolConfig.setMinIdle(0);

            poolConfig.setMaxIdle(maxIdle);

            poolConfig.setMaxWaitMillis(-1);

            poolConfig.setLifo(true);

            poolConfig.setTestOnBorrow(true);

            poolConfig.setTestWhileIdle(true);

            poolConfig.setNumTestsPerEvictionRun(1);

            poolConfig.setTimeBetweenEvictionRunsMillis(1000);

            poolConfig.setEvictionPolicy(new DefaultEvictionPolicy());

            poolConfig.setMinEvictableIdleTimeMillis(3000);

            poolConfig.setSoftMinEvictableIdleTimeMillis(3000);

            poolConfig.setBlockWhenExhausted(true);

            poolMap.putIfAbsent(key, new GenericObjectPool<ManagedChannel>
                    (new ManagedChannelFactory(host, port), poolConfig));


        }
        GenericObjectPool<ManagedChannel>   objectPool =poolMap.get(key);


        logger.info("grpc pool host {} active num {} idle num {}",key,objectPool.getNumActive(),objectPool.getNumIdle());

        return objectPool.borrowObject();
    }

    ;


    private class ManagedChannelFactory extends BasePooledObjectFactory<ManagedChannel> {

        private String host;
        private int port;

        public ManagedChannelFactory(String host, int port) {
            this.host = host;
            this.port = port;
        }

        @Override
        public ManagedChannel create() throws Exception {


            ManagedChannelBuilder builder = ManagedChannelBuilder
                    .forAddress(host,port)
                    .keepAliveTime(6, TimeUnit.SECONDS)
                    .keepAliveTimeout(6, TimeUnit.SECONDS)
                    .keepAliveWithoutCalls(true)
                    .idleTimeout(6, TimeUnit.SECONDS)
                    .perRpcBufferLimit(128 << 20)
                   // .flowControlWindow(32 << 20)
                    .maxInboundMessageSize(32 << 20)


                    .retryBufferSize(16 << 20);



            builder
                    .usePlaintext();

            ManagedChannel managedChannel = builder
                    .build();

            return ManagedChannelBuilder.forAddress(host, port).
                    usePlaintext(true).build();
        }

        @Override
        public PooledObject<ManagedChannel> wrap(ManagedChannel managedChannel) {
            return new DefaultPooledObject<>(managedChannel);
        }

        @Override
        public void destroyObject(PooledObject<ManagedChannel> p) throws Exception {
            //System.err.println("destroyObject ================");
            try {
                p.getObject().shutdownNow();

                super.destroyObject(p);
            }catch(Exception e){

            }
        }
        @Override
        public boolean validateObject(PooledObject<ManagedChannel> p) {


            ManagedChannel   managedChannel = p.getObject();


            boolean  isOk = !managedChannel.isShutdown()&&!managedChannel.isTerminated();
            System.err.println("validateObject ================"+isOk);


            return  isOk;
        }

    }


    public  static  void  main(String[]  args){




    }


}
