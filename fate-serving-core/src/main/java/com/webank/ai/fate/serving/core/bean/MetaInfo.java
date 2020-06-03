package com.webank.ai.fate.serving.core.bean;

import com.google.common.collect.Maps;
import com.webank.ai.fate.serving.core.adaptor.AdaptorDescriptor;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class MetaInfo {
    static public long currentVersion = 200;
    static public boolean PROPERTY_REMOTE_MODEL_INFERENCE_RESULT_CACHE_SWITCH = false;
    static public int PORT;
    static public String  ZK_URL;
    static public String PROPERTY_PROXY_ADDRESS;
    static public int SERVING_CORE_POOL_SIZE;
    static public int SERVING_MAX_POOL_SIZE;
    static public int SERVING_POOL_ALIVE_TIME;
    static public int SERVING_POOL_QUEUE_SIZE;
    static public boolean USE_REGISTER;
    static public String FEATURE_BATCH_ADAPTOR;
    static public String FEATURE_SINGLE_ADAPTOR;
    static public int SINGLE_INFERENCE_RPC_TIMEOUT;
    static public int BATCH_INFERENCE_RPC_TIMEOUT;
    static List<AdaptorDescriptor.ParamDescriptor> inferenceParamDescriptorList;
    static List<AdaptorDescriptor.ParamDescriptor> batchInferenceParamDescriptorList;
    static public String PROXY_ROUTER_TABLE;
    static public String PROPERTY_REDIS_IP;
    static public String PROPERTY_REDIS_PASSWORD;
    static public int PROPERTY_REDIS_PORT;
    static public int PROPERTY_REDIS_TIMEOUT;
    static public int PROPERTY_REDIS_MAX_TOTAL;
    static public int PROPERTY_REDIS_MAX_IDLE;
    static public int PROPERTY_REDIS_EXPIRE;
    static public String CACHE_TYPE;
    public static Integer PROPERTY_LOCAL_CACHE_MAXSIZE;
    public static Integer PROPERTY_LOCAL_CACHE_EXPIRE;
    public static Integer PROPERTY_LOCAL_CACHE_INTERVAL;
    public static int BATCH_SPLIT_SIZE;
    public static int LR_SPLIT_SIZE;



    public static Map toMap() {

        Map result = Maps.newHashMap();
        Field[] fields = MetaInfo.class.getFields();

        for (Field field : fields) {
            try {
                if(field.get(MetaInfo.class)!=null) {
                    result.put(field.getName(), field.get(MetaInfo.class));
                }

            } catch (IllegalAccessException e) {

            }
        }
        return result;

    }




}
