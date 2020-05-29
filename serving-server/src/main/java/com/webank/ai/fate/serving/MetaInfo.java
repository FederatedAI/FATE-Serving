package com.webank.ai.fate.serving;

import com.google.common.collect.Maps;
import com.webank.ai.fate.serving.core.adaptor.AdaptorDescriptor;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class MetaInfo {
    static public long currentVersion = 200;
    static public boolean PROPERTY_REMOTE_MODEL_INFERENCE_RESULT_CACHE_SWITCH = false;
    static public int PORT;
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

    public static Map toMap() {

        Map result = Maps.newHashMap();
        Field[] fields = MetaInfo.class.getFields();

        for (Field field : fields) {
            try {
                result.put(field.getName(), field.get(MetaInfo.class));

            } catch (IllegalAccessException e) {

            }
        }
        return result;


    }




}
