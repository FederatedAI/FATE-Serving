package com.webank.ai.fate.serving;

import com.google.common.collect.Maps;
import com.webank.ai.fate.serving.core.adaptor.AdaptorDescriptor;
import com.webank.ai.fate.serving.core.bean.Dict;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class MetaInfo {

    static public long  currentVersion = 200;
    static public long  batchRpcTimeOut=0;
    static public long  singleRpcTimeOut=0;
    static public boolean useCache =false;
    static public String  PROPERTY_PROXY_ADDRESS;
    static public int   SERVING_CORE_POOL_SIZE;
    static public int   SERVING_MAX_POOL_SIZE;
    static public int   SERVING_POOL_ALIVE_TIME;
    static public int   SERVING_POOL_QUEUE_SIZE;
    static public boolean  USE_REGISTER;
    static public String  FEATURE_BATCH_ADAPTOR;

    static List<AdaptorDescriptor.ParamDescriptor> inferenceParamDescriptorList;

    static List<AdaptorDescriptor.ParamDescriptor> batchInferenceParamDescriptorList;


    public static  Map toMap(){

        Map result = Maps.newHashMap();
        Field[] fields = MetaInfo.class.getFields();

        for( Field field : fields ){
            try {
                result.put(field.getName(),field.get(MetaInfo.class));

            } catch (IllegalAccessException e) {

            }
        }
        return  result;



    }

    //            address = environment.getProperty(Dict.PROPERTY_PROXY_ADDRESS);
//    Integer corePoolSize = environment.getProperty("serving.core.pool.size", int.class, processors);
//    Integer maxPoolSize = environment.getProperty("serving.max.pool.size", int.class, processors * 2);
//    Integer aliveTime = environment.getProperty("serving.pool.alive.time", int.class, 1000);
//    Integer queueSize = environment.getProperty("serving.pool.queue.size", int.class, 10);
//    boolean useRegister = environment.getProperty(Dict.USE_REGISTER, boolean.class, Boolean.TRUE);

//    String ip = environment.getProperty("redis.ip");
//    String password = environment.getProperty("redis.password");
//    Integer port = environment.getProperty("redis.port", Integer.class);
//    Integer timeout = environment.getProperty("redis.timeout", Integer.class, 2000);
//    Integer maxTotal = environment.getProperty("redis.maxTotal", Integer.class, 20);
//    Integer maxIdle = environment.getProperty("redis.maxIdle", Integer.class, 20);
//    Integer expire = environment.getProperty("redis.expire", Integer.class);

//    Integer maxSize = environment.getProperty("local.cache.maxsize", Integer.class, 10000);
//    Integer expireTime = environment.getProperty("local.cache.expire", Integer.class, 30);
//    Integer interval = environment.getProperty("local.cache.interval", Integer.class, 3);
//      String adaptorClass = environment.getProperty("feature.batch.adaptor");



}
