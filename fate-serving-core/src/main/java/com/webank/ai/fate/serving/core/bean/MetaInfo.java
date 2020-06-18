package com.webank.ai.fate.serving.core.bean;

import com.google.common.collect.Maps;
import com.webank.ai.fate.serving.core.adaptor.AdaptorDescriptor;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class MetaInfo {
    public static long currentVersion = 200;
    public static List<AdaptorDescriptor.ParamDescriptor> inferenceParamDescriptorList;
    public static List<AdaptorDescriptor.ParamDescriptor> batchInferenceParamDescriptorList;
    public static Boolean PROPERTY_REMOTE_MODEL_INFERENCE_RESULT_CACHE_SWITCH;
    public static Integer PROPERTY_PORT;
    public static String  PROPERTY_ZK_URL;
    public static String PROPERTY_PROXY_ADDRESS;
    public static Integer SERVING_CORE_POOL_SIZE;
    public static Integer SERVING_MAX_POOL_SIZE;
    public static Integer SERVING_POOL_ALIVE_TIME;
    public static Integer SERVING_POOL_QUEUE_SIZE;
    public static Boolean PROPERTY_USE_REGISTER;
    public static Boolean PROPERTY_USE_ZK_ROUTER;
    public static String FEATURE_BATCH_ADAPTOR;
    public static Integer BATCH_INFERENCE_MAX;
    public static String FEATURE_SINGLE_ADAPTOR;
    public static Integer SINGLE_INFERENCE_RPC_TIMEOUT;
    public static Integer BATCH_INFERENCE_RPC_TIMEOUT;
    static public String PROXY_ROUTER_TABLE;
    static public String PROPERTY_REDIS_IP;
    static public String PROPERTY_REDIS_PASSWORD;
    static public Integer PROPERTY_REDIS_PORT;
    static public Integer PROPERTY_REDIS_TIMEOUT;
    static public Integer PROPERTY_REDIS_MAX_TOTAL;
    static public Integer PROPERTY_REDIS_MAX_IDLE;
    static public Integer PROPERTY_REDIS_EXPIRE;
    static public String PROPERTY_CACHE_TYPE;
    public static Integer PROPERTY_LOCAL_CACHE_MAXSIZE;
    public static Integer PROPERTY_LOCAL_CACHE_EXPIRE;
    public static Integer PROPERTY_LOCAL_CACHE_INTERVAL;
    public static Integer BATCH_SPLIT_SIZE;
    public static Integer LR_SPLIT_SIZE;


    public static String PROPERTY_SERVICE_ROLE_NAME;
    public static String MODEL_TRANSFER_URL;



    public static Integer PROPERTY_COORDINATOR;
    public static Integer PROPERTY_SERVER_PORT;
    public static String PROPERTY_INFERENCE_SERVICE_NAME;
    public static String PROPERTY_ROUTE_TYPE;
    public static String PROPERTY_ROUTE_TABLE;
    public static String PROPERTY_AUTH_FILE;
    public static Integer PROPERTY_PROXY_GRPC_INTRA_PORT;
    public static Integer PROPERTY_PROXY_GRPC_INTER_PORT;
    public static Integer PROPERTY_PROXY_GRPC_INFERENCE_TIMEOUT;
    public static Integer PROPERTY_PROXY_GRPC_INFERENCE_ASYNC_TIMEOUT;
    public static Integer PROPERTY_PROXY_GRPC_UNARYCALL_TIMEOUT;
    public static Integer PROPERTY_PROXY_GRPC_THREADPOOL_CORESIZE;
    public static Integer PROPERTY_PROXY_GRPC_THREADPOOL_MAXSIZE;
    public static Integer PROPERTY_PROXY_GRPC_THREADPOOL_QUEUESIZE;
    public static Integer PROPERTY_PROXY_ASYNC_TIMEOUT;
    public static Integer PROPERTY_PROXY_ASYNC_CORESIZE;
    public static Integer PROPERTY_PROXY_ASYNC_MAXSIZE;
    public static Integer PROPERTY_PROXY_GRPC_BATCH_INFERENCE_TIMEOUT;


    public static Map toMap() {
        Map result = Maps.newHashMap();
        Field[] fields = MetaInfo.class.getFields();

        for (Field field : fields) {
            try {
                if (field.get(MetaInfo.class) != null) {
                    String key = Dict.class.getField(field.getName()) != null ? String.valueOf(Dict.class.getField(field.getName()).get(Dict.class)) : field.getName();
                    result.put(key, field.get(MetaInfo.class));
                }
            } catch (IllegalAccessException | NoSuchFieldException e) {

            }
        }
        return result;
    }

}
