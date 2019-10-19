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


public class Dict {

    public static final String ORIGIN_REQUEST = "origin_request";
    public static final String CASEID = "caseid";
    public static final String SCORE = "score";
    public static final String SEQNO = "seqno";
    public static final String NONE = "NONE";
    public static final String POST_PROCESSING_CONFIG = "InferencePostProcessingAdapter";
    public static final String PRE_PROCESSING_CONFIG = "InferencePreProcessingAdapter";
    public static final String GET_REMOTE_PARTY_RESULT = "getRemotePartyResult";
    public static final String FEDERATED_RESULT = "federatedResult";
    public static final String PORT = "port";

    public static final String HIT_CACHE = "hitCache";

    public static final String REQUEST_SEQNO = "REQUEST_SEQNO";

    public static final String MODEL_KEYS = "model_keys";
    public static final String MODEL_NANESPACE_DATA = "model_namespace_data";
    public static final String APPID_NANESPACE_DATA = "appid_namespace_data";
    public static final String PARTNER_MODEL_DATA = "partner_model_index";
    public static final String MODEL_FEDERATED_PARTY = "model_federated_party";
    public static final String MODEL_FEDERATED_ROLES = "model_federated_roles";
    public static final String VERSION ="version";

    // configuration property key
    public static final String PROPERTY_REMOTE_MODEL_INFERENCE_RESULT_CACHE_TTL = "remoteModelInferenceResultCacheTTL";
    public static final String PROPERTY_REMOTE_MODEL_INFERENCE_RESULT_CACHE_MAX_SIZE = "remoteModelInferenceResultCacheMaxSize";
    public static final String PROPERTY_INFERENCE_RESULT_CACHE_TTL = "inferenceResultCacheTTL";
    public static final String PROPERTY_INFERENCE_RESULT_CACHE_CACHE_MAX_SIZE = "inferenceResultCacheCacheMaxSize";
    public static final String PROPERTY_REDIS_MAXTOTAL = "redis.maxTotal";
    public static final String PROPERTY_REDIS_MAXIDLE = "redis.maxIdle";
    public static final String PROPERTY_REDIS_IP = "redis.ip";
    public static final String PROPERTY_REDIS_PORT = "redis.port";
    public static final String PROPERTY_REDIS_TIMEOUT = "redis.timeout";
    public static final String PROPERTY_REDIS_PASSWORD = "redis.password";
    public static final String PROPERTY_EXTERNAL_INFERENCE_RESULT_CACHE_DB_INDEX = "external.inferenceResultCacheDBIndex";
    public static final String PROPERTY_EXTERNAL_INFERENCE_RESULT_CACHE_TTL = "external.inferenceResultCacheTTL";
    public static final String PROPERTY_EXTERNAL_REMOTE_MODEL_INFERENCE_RESULT_CACHE_DB_INDEX = "external.remoteModelInferenceResultCacheDBIndex";
    public static final String PROPERTY_EXTERNAL_REMOTE_MODEL_INFERENCE_RESULT_CACHE_TTL = "external.remoteModelInferenceResultCacheTTL";
    public static final String PROPERTY_REMOTE_MODEL_INFERENCE_RESULT_CACHE_SWITCH = "remoteModelInferenceResultCacheSwitch";
    public static final String PROPERTY_CAN_CACHE_RET_CODE = "canCacheRetcode";
    public static final String PROPERTY_SERVICE_ROLE_NAME = "serviceRoleName";
    public static final String PROPERTY_SERVICE_ROLE_NAME_DEFAULT_VALUE = "serving-1.0";
    public static final String PROPERTY_ONLINE_DATA_ACCESS_ADAPTER = "OnlineDataAccessAdapter";
    public static final String PROPERTY_MODEL_CACHE_ACCESS_TTL = "modelCacheAccessTTL";
    public static final String PROPERTY_MODEL_CACHE_MAX_SIZE = "modelCacheMaxSize";
    public static final String PROPERTY_INFERENCE_WORKER_THREAD_NUM = "inferenceWorkerThreadNum";
    public static final String PROPERTY_PROXY_ADDRESS = "proxy";
    public static final String ONLINE_ENVIROMMENT = "online";
    public static final String PROPERTY_ROLL_ADDRESS = "roll";
    public static final String PROPERTY_USE_ZOOKEEPER = "useZookeeper";
    public static final String PROPERTY_SERVER_PORT = "port";
    public static final String PROPERTY_USER_DIR = "user.dir";
    public static final String PROPERTY_USER_HOME = "user.home";

    public static final String ACTION_TYPE_ASYNC_EXECUTE = "ASYNC_EXECUTE";

    public static final String RET_CODE = "retcode";
    public static final String DATA = "data";
    public static final String STATUS = "status";
    public static final String SUCCESS = "success";
    public static final String PROB = "prob";
    public static final String ACCESS = "access";
    public static final String MODEL_WRIGHT_HIT_RATE = "modelWrightHitRate";
    public static final String INPUT_DATA_HIT_RATE = "inputDataHitRate";
    public static final String GUEST_MODEL_WEIGHT_HIT_RATE = "guestModelWeightHitRate";
    public static final String GUEST_INPUT_DATA_HIT_RATE = "guestInputDataHitRate";
    public static final String MIN_MAX_SCALE = "min_max_scale";
    public static final String STANDARD_SCALE = "standard_scale";
    public static final String DSL_COMPONENTS = "components";
    public static final String DSL_CODE_PATH = "CodePath";
    public static final String DSL_INPUT = "input";
    public static final String DSL_DATA = "data";
    public static final String DSL_ARGS = "args";
    public static final String HOST = "host";
    public static final String GUEST = "guest";
    public static final String PARTNER_PARTY_NAME = "partnerPartyName";
    public static final String PARTY_NAME = "partyName";
    public static final String MY_PARTY_NAME = "myPartyName";
    public static final String FEDERATED_INFERENCE = "federatedInference";
    public static final String FEDERATED_INFERENCE_FOR_TREE = "federatedInference4Tree";
    public static final String DEVICE_ID = "device_id";
    public static final String PHONE_NUM = "phone_num";
    public static final String FEDERATED_PARAMS = "federatedParams";
    public static final String COMMIT_ID = "commitId";
    public static final String BRANCH_MASTER = "master";
    public static final String INFERENCE_AUDIT = "inferenceAudit";
    public static final String INFERENCE = "inference";
    public static final String INFERENCE_REQUEST = "inferenceRequest";
    public static final String INFERENCE_RESULT = "inferenceResult";

    public static final String CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_TYPE_JSON_UTF8 = "application/json;charset=UTF-8";
    public static final String CHARSET_UTF8 = "UTF-8";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    public static final String TAG = "tag";
    public static final String INPUT_DATA = "input_data";
    public static final String COMPONENT_NAME = "componentName";
    public static final String TREE_COMPUTE_ROUND = "treeComputeRound";
    public static final String SERVICE_NAME = "serviceName";
    public static final String TREE_LOCATION = "treeLocation";

    public static final String UNARYCALL = "unaryCall";
    public static final String USE_ZK_ROUTER = "useZkRouter";
    public static final String FALSE = "false";
    public static final String USE_REGISTER = "useRegister";
    public static final String USE_JMX = "useJMX";
    public static final String JMX_SERVER_NAME = "jmx.server.name";
    public static final String JMX_PORT = "jmx.port";


}
