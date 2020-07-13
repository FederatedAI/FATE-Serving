package com.webank.ai.fate.serving.cache;

import com.google.common.collect.Lists;
import io.lettuce.core.LettuceFutures;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LettuceClientTest {

    static Logger logger = LoggerFactory.getLogger(LettuceClientTest.class);

    private static String CLUSTER_NODES = "127.0.0.1:6379,127.0.0.1:6380,127.0.0.1:6381,127.0.0.1:6382,127.0.0.1:6383,127.0.0.1:6384";
    private static String PASSWORD = null;
    private static List<RedisURI> NODES = Lists.newArrayList();

    @BeforeClass
    public static void BeforeClass() {
        logger.info("======== before =========");
    }

    @AfterClass
    public static void afterClass() {
        logger.info("======== after =========");
    }

    @Test
    public void testStandalone() {
        // Syntax: redis://[password@]host[:port][/databaseNumber]
//        RedisClient redisClient = RedisClient.create("redis://@127.0.0.1:6379/0");
        RedisURI redisUri = RedisURI.builder().withHost("localhost").withPort(6379)
                .withTimeout(Duration.of(10, ChronoUnit.SECONDS))
                .build();
        RedisClient redisClient = RedisClient.create(redisUri);
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();

        syncCommands.set("hello", "Hello, Redis!");
        syncCommands.set("lettuce", "Hello, lettuce!");
        syncCommands.expire("hello", 30); // SECOND

        logger.info("===== PING ====== {}", syncCommands.ping());
        logger.info("===== TEST GET ==== {}", syncCommands.get("hello"));
        logger.info("===== TEST KEYS ==== {}", syncCommands.keys("*"));
        logger.info("===== TEST TTL ==== {}", syncCommands.ttl("hello"));
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("===== TEST TTL ==== {}", syncCommands.ttl("hello"));
        logger.info("===== TEST DEL ==== {}", syncCommands.del("hello"));
        logger.info("===== TEST GET ==== {}", syncCommands.get("hello"));

        connection.close();
        redisClient.shutdown();
    }

    @Test
    public void testStandaloneAsync() throws Exception {
        RedisURI redisUri = RedisURI.builder().withHost("localhost").withPort(6379).withTimeout(Duration.of(10, ChronoUnit.SECONDS)).build();
        RedisClient redisClient = RedisClient.create(redisUri);
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisAsyncCommands<String, String> asyncCommands = connection.async();

        RedisFuture<String> future = asyncCommands.set("async", "Hello, async!");
        future.thenAccept(value -> logger.info("===== TEST THEN ACCEPT ==== {}", value));
        logger.info("===== TEST GET ==== {}", future.get());
        logger.info("===== TEST GET ==== {}", future.get());

        asyncCommands.set("thenAcceptBoth", "throwable").thenAcceptBoth(asyncCommands.get("thenAcceptBoth"), (s, g) -> {
            logger.info("===== TEST SET ==== {}", s);
            logger.info("===== TEST GET ==== {}", g);
        });

        connection.close();
        redisClient.shutdown();
    }

    /**
     * batch operation
     */
    @Test
    public void testAsyncManualFlush() {
        RedisURI redisUri = RedisURI.builder().withHost("localhost").withPort(6379).withTimeout(Duration.of(10, ChronoUnit.SECONDS)).build();
        RedisClient redisClient = RedisClient.create(redisUri);
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisAsyncCommands<String, String> asyncCommands = connection.async();

        asyncCommands.setAutoFlushCommands(false);
        List<RedisFuture<?>> redisFutures = Lists.newArrayList();
        int count = 100;
        for (int i = 0; i < count; i++) {
            String key = "key-" + (i + 1);
            String value = "value-" + (i + 1);
            redisFutures.add(asyncCommands.set(key, value));
            redisFutures.add(asyncCommands.expire(key, 20));
        }
        long start = System.currentTimeMillis();
        asyncCommands.flushCommands();
        boolean result = LettuceFutures.awaitAll(10, TimeUnit.SECONDS, redisFutures.toArray(new RedisFuture[0]));
        logger.info("Lettuce cost:{} ms", System.currentTimeMillis() - start);

        connection.close();
        redisClient.shutdown();
    }

    private void initCluster() {
        if (StringUtils.isNotBlank(CLUSTER_NODES)) {
            String[] clusterNode = CLUSTER_NODES.split(",");
            if (clusterNode.length > 0) {
                for (String hostPort : clusterNode) {
                    String[] node = hostPort.split(":");
                    NODES.add(RedisURI.Builder.redis(node[0], Integer.valueOf(node[1])).withPassword(PASSWORD).build());
                }
            }
        }
    }

    @Test
    public void testCluster() {
        initCluster();

        RedisClusterClient clusterClient = RedisClusterClient.create(NODES);
        StatefulRedisClusterConnection<String, String> connection = clusterClient.connect();
        RedisAdvancedClusterCommands<String, String> syncCommands = connection.sync();

        syncCommands.set("hello", "Hello, Redis!");
        syncCommands.set("lettuce", "Hello, lettuce!");
        syncCommands.expire("hello", 60000);

        logger.info("===== TEST GET ==== {}", syncCommands.get("hello"));
        logger.info("===== TEST KEYS ==== {}", syncCommands.keys("*"));
        logger.info("===== TEST TTL ==== {}", syncCommands.ttl("hello"));
        logger.info("===== TEST DEL ==== {}", syncCommands.del("hello"));
        logger.info("===== TEST GET ==== {}", syncCommands.get("hello"));

    }
}
