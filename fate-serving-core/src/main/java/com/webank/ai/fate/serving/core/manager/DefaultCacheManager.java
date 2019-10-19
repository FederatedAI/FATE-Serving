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

package com.webank.ai.fate.serving.core.manager;

import com.google.common.base.Charsets;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.hash.Hashing;
import com.webank.ai.fate.core.bean.ReturnResult;
import com.webank.ai.fate.core.utils.Configuration;
import com.webank.ai.fate.core.utils.ObjectTransform;
import com.webank.ai.fate.serving.core.bean.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Pipeline;
import com.webank.ai.fate.serving.core.bean.CacheManager;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class DefaultCacheManager implements CacheManager, InitializingBean {

    private final Logger LOGGER = LogManager.getLogger();
    private JedisPool jedisPool;
    private Cache<String, ReturnResult> inferenceResultCache;
    private Cache<String, ReturnResult> remoteModelInferenceResultCache;
    private Cache<String, Object> processDataCache;
    private int[] remoteModelInferenceResultCacheDBIndex;
    private int[] inferenceResultCacheDBIndex;
    private int[] processCacheDBIndex;


    private int externalRemoteModelInferenceResultCacheTTL;
    private int externalInferenceResultCacheTTL;
    private Set<Integer> canCacheRetcode;

    DefaultCacheManager() {
        remoteModelInferenceResultCache = CacheBuilder.newBuilder()
                .expireAfterAccess(Configuration.getPropertyInt("remoteModelInferenceResultCacheTTL"), TimeUnit.SECONDS)
                .maximumSize(Configuration.getPropertyInt("remoteModelInferenceResultCacheMaxSize"))
                .build();


        processDataCache = CacheBuilder.newBuilder()
                .expireAfterAccess(60, TimeUnit.SECONDS)
                .maximumSize(50000)
                .build();


        inferenceResultCache = CacheBuilder.newBuilder()
                .expireAfterAccess(Configuration.getPropertyInt("inferenceResultCacheTTL"), TimeUnit.SECONDS)
                .maximumSize(Configuration.getPropertyInt("inferenceResultCacheCacheMaxSize"))
                .build();

        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(Configuration.getPropertyInt("redis.maxTotal"));
        jedisPoolConfig.setMaxIdle(Configuration.getPropertyInt("redis.maxIdle"));
        jedisPool = new JedisPool(jedisPoolConfig,
                Configuration.getProperty("redis.ip"),
                Configuration.getPropertyInt("redis.port"),
                Configuration.getPropertyInt("redis.timeout"),
                Configuration.getProperty("redis.password"));


        inferenceResultCacheDBIndex = initializeCacheDBIndex(Configuration.getProperty("external.inferenceResultCacheDBIndex"));
        externalInferenceResultCacheTTL = Configuration.getPropertyInt("external.inferenceResultCacheTTL");
        remoteModelInferenceResultCacheDBIndex = initializeCacheDBIndex(Configuration.getProperty("external.remoteModelInferenceResultCacheDBIndex"));
        processCacheDBIndex = initializeCacheDBIndex(Configuration.getProperty("external.processCacheDBIndex"));
        externalRemoteModelInferenceResultCacheTTL = Configuration.getPropertyInt("external.remoteModelInferenceResultCacheTTL");
        canCacheRetcode = initializeCanCacheRetcode();
    }

    @Override
    public void afterPropertiesSet() throws Exception {


    }

    @Override
    public void store(Context context, String key, Object object) {

        LOGGER.info("store key {} value {}", key, object);
        CacheValueConfig cacheValueConfig = getCacheValueConfig(key, CacheType.PROCESS_DATA);
        putIntoRedisCache(key, cacheValueConfig, object);


    }

    @Override
    public <T> T restore(Context context, String key, Class<T> dataType) {

        CacheValueConfig cacheValueConfig = getCacheValueConfig(key, CacheType.PROCESS_DATA);
        T result = getFromRedisCache(key, cacheValueConfig, dataType);
        LOGGER.info("restore key {} value {}", key, result);
        return result;
    }

    @Override
    public void putInferenceResultCache(Context context, String partyId, String caseid, ReturnResult returnResult) {

        long beginTime = System.currentTimeMillis();
        try {

            String inferenceResultCacheKey = generateInferenceResultCacheKey(partyId, caseid);
            boolean putCacheSuccess = putIntoCache(inferenceResultCacheKey, CacheType.INFERENCE_RESULT, returnResult);
            if (putCacheSuccess) {
                LOGGER.info("Put {} inference result into cache", inferenceResultCacheKey);
            }
        } finally {
            long end = System.currentTimeMillis();
            LOGGER.info("caseid {} putInferenceResultCache cost {}", context.getCaseId(), end - beginTime);

        }
    }

    @Override
    public ReturnResult getInferenceResultCache(String partyId, String caseid) {
        String inferenceResultCacheKey = generateInferenceResultCacheKey(partyId, caseid);
        ReturnResult returnResult = getFromCache(inferenceResultCacheKey, CacheType.INFERENCE_RESULT);
        if (returnResult != null) {
            LOGGER.info("Get {} inference result from cache.", inferenceResultCacheKey);
        }
        return returnResult;
    }

    @Override
    public void putRemoteModelInferenceResult(FederatedParty remoteParty, FederatedRoles federatedRoles, Map<String, Object> featureIds, ReturnResult returnResult) {
        if (!Boolean.parseBoolean(Configuration.getProperty("remoteModelInferenceResultCacheSwitch"))) {
            return;
        }
        String remoteModelInferenceResultCacheKey = generateRemoteModelInferenceResultCacheKey(remoteParty, federatedRoles, featureIds);
        boolean putCacheSuccess = putIntoCache(remoteModelInferenceResultCacheKey, CacheType.REMOTE_MODEL_INFERENCE_RESULT, returnResult);
        if (putCacheSuccess) {
            LOGGER.info("Put {} remote model inference result into cache.", remoteModelInferenceResultCacheKey);
        }
    }

    @Override
    public ReturnResult getRemoteModelInferenceResult(FederatedParty remoteParty, FederatedRoles federatedRoles, Map<String, Object> featureIds) {
        if (!Boolean.parseBoolean(Configuration.getProperty("remoteModelInferenceResultCacheSwitch"))) {
            return null;
        }
        String remoteModelInferenceResultCacheKey = generateRemoteModelInferenceResultCacheKey(remoteParty, federatedRoles, featureIds);
        ReturnResult returnResult = getFromCache(remoteModelInferenceResultCacheKey, CacheType.REMOTE_MODEL_INFERENCE_RESULT);
        if (returnResult != null) {
            LOGGER.info("Get {} remote model inference result from cache.", remoteModelInferenceResultCacheKey);
        }
        return returnResult;
    }

    private ReturnResult getFromCache(String cacheKey, CacheType cacheType) {
        CacheValueConfig cacheValueConfig = getCacheValueConfig(cacheKey, cacheType);
        ReturnResult returnResultFromInCache = (ReturnResult) cacheValueConfig.getInProcessCache().getIfPresent(cacheKey);
        if (returnResultFromInCache != null) {
            return returnResultFromInCache;
        }
        ReturnResult returnResultFromExternalCache = getFromRedisCache(cacheKey, cacheValueConfig, ReturnResult.class);
        if (returnResultFromExternalCache != null) {
            cacheValueConfig.getInProcessCache().put(cacheKey, returnResultFromExternalCache);
        }
        return returnResultFromExternalCache;
    }

    private boolean putIntoCache(String cacheKey, CacheType cacheType, ReturnResult returnResult) {
        CacheValueConfig cacheValueConfig = getCacheValueConfig(cacheKey, cacheType);
        if (canCacheRetcode.contains(returnResult.getRetcode())) {
            cacheValueConfig.getInProcessCache().put(cacheKey, returnResult);
            putIntoRedisCache(cacheKey, cacheValueConfig, returnResult);
            return true;
        } else {
            return false;
        }
    }

    private <T> T getFromRedisCache(String cacheKey, CacheValueConfig cacheValueConfig, Class<T> dataType) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(cacheValueConfig.getDbIndex());
            String cacheValueString = jedis.get(cacheKey);
            T returnResultFromExternalCache = (T) ObjectTransform.json2Bean(cacheValueString, dataType);
            return returnResultFromExternalCache;
        }

    }

    private void putIntoRedisCache(String cacheKey, CacheValueConfig cacheValueConfig, Object returnResult) {
        try (Jedis jedis = jedisPool.getResource()) {
            Pipeline redisPipeline = jedis.pipelined();
            redisPipeline.select(cacheValueConfig.getDbIndex());
            redisPipeline.set(cacheKey, ObjectTransform.bean2Json(returnResult));
            redisPipeline.expire(cacheKey, cacheValueConfig.getTtl());
            redisPipeline.sync();


        }
    }

    private int[] initializeCacheDBIndex(String config) {
        int[] dbIndexs;
        String[] indexStartEnd = config.split(",");
        if (indexStartEnd.length > 1) {
            int start = Integer.parseInt(indexStartEnd[0]);
            int end = Integer.parseInt(indexStartEnd[1]);
            dbIndexs = new int[end - start + 1];
            for (int i = 0; i < end; i++) {
                dbIndexs[i] = start + i;
            }
        } else {
            dbIndexs = new int[1];
            dbIndexs[0] = Integer.parseInt(indexStartEnd[0]);
        }
        return dbIndexs;
    }

    private Set<Integer> initializeCanCacheRetcode() {
        Set<Integer> retcodes = new HashSet<>();
        String[] retcodeString = Configuration.getProperty("canCacheRetcode").split(",");
        for (int i = 0; i < retcodeString.length; i++) {
            retcodes.add(Integer.parseInt(retcodeString[i]));
        }
        return retcodes;
    }

    private CacheValueConfig getCacheValueConfig(String cacheKey, CacheType cacheType) {
        int dbIndex;
        int ttl;
        switch (cacheType) {
            case INFERENCE_RESULT:
                dbIndex = getCacheDBIndex(cacheKey, inferenceResultCacheDBIndex);
                ttl = externalInferenceResultCacheTTL + new Random().nextInt(10);
                return new CacheValueConfig<>(dbIndex, ttl, inferenceResultCache);
            case REMOTE_MODEL_INFERENCE_RESULT:
                dbIndex = getCacheDBIndex(cacheKey, remoteModelInferenceResultCacheDBIndex);
                ttl = externalRemoteModelInferenceResultCacheTTL + new Random().nextInt(100);
                return new CacheValueConfig<>(dbIndex, ttl, remoteModelInferenceResultCache);
            case PROCESS_DATA:
                dbIndex = getCacheDBIndex(cacheKey, processCacheDBIndex);
                ttl = 60;
                return new CacheValueConfig<>(dbIndex, ttl, processDataCache);
            default:
                return null;
        }
    }

    private int getCacheDBIndex(String cacheKey, int[] dbIndexs) {
        int i = Hashing.murmur3_128().hashString(cacheKey, Charsets.UTF_8).asInt() % dbIndexs.length;
        return dbIndexs[i > 0 ? i : -i];
    }

    private String generateInferenceResultCacheKey(String partyId, String caseid) {
        return StringUtils.join(Arrays.asList(partyId, caseid), "_");
    }

    private String generateRemoteModelInferenceResultCacheKey(FederatedParty remoteParty, FederatedRoles federatedRoles, Map<String, Object> featureIds) {
        String remotePartyKey = StringUtils.join(Arrays.asList(remoteParty.getRole(), remoteParty.getPartyId(), FederatedUtils.federatedRolesIdentificationString(federatedRoles)), "#");
        Object[] featureIdKeys = featureIds.keySet().toArray();
        Arrays.sort(featureIdKeys);
        List<String> featureIdItemString = new ArrayList<>();
        for (int i = 0; i < featureIdKeys.length; i++) {
            featureIdItemString.add(StringUtils.join(Arrays.asList(featureIdKeys[i], featureIds.get(featureIdKeys[i])), ":"));
        }
        String featureIdString = StringUtils.join(featureIdItemString, "_");
        String cacheKey;
        cacheKey = StringUtils.join(Arrays.asList(remotePartyKey, featureIdString), "#");
        LOGGER.info(cacheKey);
        return cacheKey;
    }

    private enum CacheType {
        INFERENCE_RESULT,
        REMOTE_MODEL_INFERENCE_RESULT,
        PROCESS_DATA

    }


}
