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

import com.google.common.collect.Maps;
import com.webank.ai.fate.serving.core.adaptor.AdaptorDescriptor;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class MetaInfo {
    public static final long CURRENT_VERSION = 216;
    public static List<AdaptorDescriptor.ParamDescriptor> inferenceParamDescriptorList;
    public static List<AdaptorDescriptor.ParamDescriptor> batchInferenceParamDescriptorList;
    public static Boolean PROPERTY_REMOTE_MODEL_INFERENCE_RESULT_CACHE_SWITCH;
    public static Integer PROPERTY_PORT;
    public static String PROPERTY_ZK_URL;
    public static String PROPERTY_PROXY_ADDRESS;
    public static Integer PROPERTY_SERVING_CORE_POOL_SIZE;
    public static Integer PROPERTY_SERVING_MAX_POOL_SIZE;
    public static Integer PROPERTY_SERVING_POOL_ALIVE_TIME;
    public static Integer PROPERTY_SERVING_POOL_QUEUE_SIZE;
    public static Boolean PROPERTY_USE_REGISTER;
    public static Boolean PROPERTY_USE_ZK_ROUTER;
    public static String PROPERTY_FEATURE_BATCH_ADAPTOR;
    public static String PROPERTY_FETTURE_BATCH_SINGLE_ADAPTOR;
    public static Integer PROPERTY_BATCH_INFERENCE_MAX;
    public static String PROPERTY_FEATURE_SINGLE_ADAPTOR;
    public static Integer PROPERTY_SINGLE_INFERENCE_RPC_TIMEOUT;
    public static Integer PROPERTY_BATCH_INFERENCE_RPC_TIMEOUT;
    public static String PROXY_ROUTER_TABLE;
    public static String PROPERTY_REDIS_IP;
    public static String PROPERTY_REDIS_PASSWORD;
    public static Integer PROPERTY_REDIS_PORT;
    public static Integer PROPERTY_REDIS_TIMEOUT;
    public static Integer PROPERTY_REDIS_MAX_TOTAL;
    public static Integer PROPERTY_REDIS_MAX_IDLE;
    public static Integer PROPERTY_REDIS_EXPIRE;
    public static String PROPERTY_REDIS_CLUSTER_NODES;
    public static String PROPERTY_CACHE_TYPE;
    public static Integer PROPERTY_LOCAL_CACHE_MAXSIZE;
    public static Integer PROPERTY_LOCAL_CACHE_EXPIRE;
    public static Integer PROPERTY_LOCAL_CACHE_INTERVAL;
    public static Integer PROPERTY_BATCH_SPLIT_SIZE;
    public static Integer PROPERTY_LR_SPLIT_SIZE;
    public static String PROPERTY_SERVICE_ROLE_NAME;
    public static String PROPERTY_MODEL_TRANSFER_URL;
    public static boolean PROPERTY_MODEL_SYNC;
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
    public static String PROPERTY_MODEL_CACHE_PATH;
    public static String PROPERTY_FATEFLOW_LOAD_URL;
    public static String PROPERTY_FATEFLOW_BIND_URL;
    public static Integer PROPERTY_GRPC_TIMEOUT;
    public static Boolean PROPERTY_ACL_ENABLE;
    public static String PROPERTY_ACL_USERNAME;
    public static String PROPERTY_ACL_PASSWORD;
    public static String PROPERTY_ROOT_PATH;
    public static Boolean PROPERTY_PRINT_INPUT_DATA;
    public static Boolean PROPERTY_PRINT_OUTPUT_DATA;
    public static Boolean PROPERTY_LR_USE_PARALLEL;
    public static Boolean PROPERTY_AUTH_OPEN;
    public static String PROPERTY_PROXY_GRPC_INTER_NEGOTIATIONTYPE;
    public static String PROPERTY_PROXY_GRPC_INTER_CA_FILE;
    public static String PROPERTY_PROXY_GRPC_INTER_CLIENT_CERTCHAIN_FILE;
    public static String PROPERTY_PROXY_GRPC_INTER_CLIENT_PRIVATEKEY_FILE;
    public static String PROPERTY_PROXY_GRPC_INTER_SERVER_CERTCHAIN_FILE;
    public static String PROPERTY_PROXY_GRPC_INTER_SERVER_PRIVATEKEY_FILE;
    public static Integer PROPERTY_ADMIN_HEALTH_CHECK_TIME;
    public static Boolean PROPERTY_ALLOW_HEALTH_CHECK;
    public static Integer HTTP_CLIENT_CONFIG_CONN_REQ_TIME_OUT;
    public static Integer HTTP_CLIENT_CONFIG_CONN_TIME_OUT;
    public static Integer HTTP_CLIENT_CONFIG_SOCK_TIME_OUT;
    public static Integer HTTP_CLIENT_INIT_POOL_MAX_TOTAL;
    public static Integer HTTP_CLIENT_INIT_POOL_DEF_MAX_PER_ROUTE;
    public static Integer HTTP_CLIENT_INIT_POOL_SOCK_TIME_OUT;
    public static Integer HTTP_CLIENT_INIT_POOL_CONN_TIME_OUT;
    public static Integer HTTP_CLIENT_INIT_POOL_CONN_REQ_TIME_OUT;
    public static Integer HTTP_CLIENT_TRAN_CONN_REQ_TIME_OUT;
    public static Integer HTTP_CLIENT_TRAN_CONN_TIME_OUT;
    public static Integer HTTP_CLIENT_TRAN_SOCK_TIME_OUT;

    public static int PROPERTY_HTTP_CONNECT_REQUEST_TIMEOUT;
    public static int PROPERTY_HTTP_CONNECT_TIMEOUT;
    public static int PROPERTY_HTTP_SOCKET_TIMEOUT;
    public static int PROPERTY_HTTP_MAX_POOL_SIZE;
    public static String PROPERTY_HTTP_ADAPTER_URL;

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
