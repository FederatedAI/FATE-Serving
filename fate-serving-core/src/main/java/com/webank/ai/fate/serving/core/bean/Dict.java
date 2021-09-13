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
    public static final String PIPLELINE_IN_MODEL = "pipeline.pipeline:Pipeline";
    public static final String POST_PROCESSING_CONFIG = "InferencePostProcessingAdapter";
    public static final String PRE_PROCESSING_CONFIG = "InferencePreProcessingAdapter";
    public static final String GET_REMOTE_PARTY_RESULT = "getRemotePartyResult";
    public static final String FEDERATED_RESULT = "federatedResult";
    public static final String PORT = "port";
    public static final String INSTANCE_ID = "instanceId";
    public static final String ORIGINAL_PREDICT_DATA = "originalPredictData";
    public static final String HIT_CACHE = "hitCache";

    public static final String REQUEST_SEQNO = "REQUEST_SEQNO";

    public static final String MODEL_KEYS = "model_keys";
    public static final String MODEL_NANESPACE_DATA = "model_namespace_data";
    public static final String APPID_NANESPACE_DATA = "appid_namespace_data";
    public static final String PARTNER_MODEL_DATA = "partner_model_index";
    public static final String MODEL_FEDERATED_PARTY = "model_federated_party";
    public static final String MODEL_FEDERATED_ROLES = "model_federated_roles";
    public static final String MODEL = "model";
    public static final String VERSION = "version";
    public static final String GRPC_TYPE = "grpcType";
    public static final String ROUTER_INFO = "routerInfo";
    public static final String RESULT_DATA = "resultData";
    public static final String RETURN_CODE = "returnCode";
    public static final String DOWN_STREAM_COST = "downstreamCost";
    public static final String DOWN_STREAM_BEGIN = "downstreamBegin";
    public static final String ROUTE_BASIS = "routeBasis";
    public static final String SOURCE_IP = "sourceIp";
    public static final String PROPERTY_SERVING_CORE_POOL_SIZE = "serving.core.pool.size";
    public static final String SERVING_MAX_POOL_ZIE = "serving.max.pool.size";
    public static final String PROPERTY_SERVING_POOL_ALIVE_TIME = "serving.pool.alive.time";
    public static final String PROPERTY_SERVING_POOL_QUEUE_SIZE = "serving.pool.queue.size";
    public static final String PROPERTY_SINGLE_INFERENCE_RPC_TIMEOUT = "single.inference.rpc.timeout";
    public static final String PROPERTY_BATCH_INFERENCE_RPC_TIMEOUT = "batch.inference.rpc.timeout";
    public static final String PROPERTY_FEATURE_SINGLE_ADAPTOR = "feature.single.adaptor";
    public static final String PROPERTY_BATCH_SPLIT_SIZE = "batch.split.size";
    public static final String PROPERTY_LR_SPLIT_SIZE = "lr.split.size";
    public static final String CACHE_TYPE_REDIS = "redis";
    public static final String DEFAULT_FATE_ROOT = "FATE-SERVICES";


    /**
     * configuration property key
     */
    public static final String PROPERTY_REMOTE_MODEL_INFERENCE_RESULT_CACHE_TTL = "remoteModelInferenceResultCacheTTL";
    public static final String PROPERTY_REMOTE_MODEL_INFERENCE_RESULT_CACHE_MAX_SIZE = "remoteModelInferenceResultCacheMaxSize";
    public static final String PROPERTY_INFERENCE_RESULT_CACHE_TTL = "inferenceResultCacheTTL";
    public static final String PROPERTY_INFERENCE_RESULT_CACHE_CACHE_MAX_SIZE = "inferenceResultCacheCacheMaxSize";
    public static final String PROPERTY_CACHE_TYPE = "cache.type";
    public static final String PROPERTY_REDIS_MAX_TOTAL = "redis.maxTotal";
    public static final String PROPERTY_REDIS_MAX_IDLE = "redis.maxIdle";
    public static final String PROPERTY_REDIS_IP = "redis.ip";
    public static final String PROPERTY_REDIS_PORT = "redis.port";
    public static final String PROPERTY_REDIS_TIMEOUT = "redis.timeout";
    public static final String PROPERTY_REDIS_PASSWORD = "redis.password";
    public static final String PROPERTY_REDIS_EXPIRE = "redis.expire";
    public static final String PROPERTY_REDIS_CLUSTER_NODES = "redis.cluster.nodes";
    public static final String PROPERTY_LOCAL_CACHE_MAXSIZE = "local.cache.maxsize";
    public static final String PROPERTY_LOCAL_CACHE_EXPIRE = "local.cache.expire";
    public static final String PROPERTY_LOCAL_CACHE_INTERVAL = "local.cache.interval";
    public static final String PROPERTY_FATEFLOW_LOAD_URL = "fateflow.load.url";
    public static final String PROPERTY_FATEFLOW_BIND_URL = "fateflow.bind.url";
    public static final String PROPERTY_GRPC_TIMEOUT = "grpc.timeout";
    public static final String PROPERTY_EXTERNAL_INFERENCE_RESULT_CACHE_DB_INDEX = "external.inferenceResultCacheDBIndex";
    public static final String PROPERTY_EXTERNAL_INFERENCE_RESULT_CACHE_TTL = "external.inferenceResultCacheTTL";
    public static final String PROPERTY_EXTERNAL_REMOTE_MODEL_INFERENCE_RESULT_CACHE_DB_INDEX = "external.remoteModelInferenceResultCacheDBIndex";
    public static final String PROPERTY_EXTERNAL_PROCESS_CACHE_DB_INDEX = "external.processCacheDBIndex";
    public static final String PROPERTY_EXTERNAL_REMOTE_MODEL_INFERENCE_RESULT_CACHE_TTL = "external.remoteModelInferenceResultCacheTTL";
    public static final String PROPERTY_REMOTE_MODEL_INFERENCE_RESULT_CACHE_SWITCH = "remoteModelInferenceResultCacheSwitch";
    public static final String PROPERTY_CAN_CACHE_RET_CODE = "canCacheRetcode";
    public static final String PROPERTY_SERVICE_ROLE_NAME = "serviceRoleName";
    public static final String PROPERTY_SERVICE_ROLE_NAME_DEFAULT_VALUE = "serving";
    public static final String PROPERTY_ONLINE_DATA_ACCESS_ADAPTER = "OnlineDataAccessAdapter";
    public static final String PROPERTY_ONLINE_DATA_BATCH_ACCESS_ADAPTER = "OnlineDataBatchAccessAdapter";
    public static final String PROPERTY_MODEL_CACHE_ACCESS_TTL = "modelCacheAccessTTL";
    public static final String PROPERTY_MODEL_CACHE_MAX_SIZE = "modelCacheMaxSize";
    public static final String PROPERTY_INFERENCE_WORKER_THREAD_NUM = "inferenceWorkerThreadNum";
    public static final String PROPERTY_PROXY_ADDRESS = "proxy";
    public static final String ONLINE_ENVIRONMENT = "online";
    public static final String PROPERTY_ROLL_ADDRESS = "roll";
    public static final String PROPERTY_FLOW_ADDRESS = "flow";
    public static final String PROPERTY_SERVING_ADDRESS = "serving";
    public static final String PROPERTY_USE_ZOOKEEPER = "useZookeeper";
    public static final String PROPERTY_PORT = "port";
    public static final String PROPERTY_USER_DIR = "user.dir";
    public static final String PROPERTY_USER_HOME = "user.home";
    public static final String PROPERTY_FILE_SEPARATOR = "file.separator";
    public static final String PROPERTY_ZK_URL = "zk.url";
    public static final String PROPERTY_USE_ZK_ROUTER = "useZkRouter";
    public static final String PROPERTY_USE_REGISTER = "useRegister";
    public static final String PROPERTY_MODEL_TRANSFER_URL = "model.transfer.url";
    public static final String PROPERTY_MODEL_SYNC = "model.synchronize";
    public static final String PROPERTY_SERVING_MAX_POOL_SIZE = "serving.max.pool.size";
    public static final String PROPERTY_FEATURE_BATCH_ADAPTOR = "feature.batch.adaptor";
    public static final String PROPERTY_ACL_ENABLE = "acl.enable";
    public static final String PROPERTY_ACL_USERNAME = "acl.username";
    public static final String PROPERTY_ACL_PASSWORD = "acl.password";
    public static final String PROXY_ROUTER_TABLE = "proxy.router.table";
    public static final String PROPERTY_BATCH_INFERENCE_MAX = "batch.inference.max";
    public static final String PROPERTY_PRINT_INPUT_DATA = "print.input.data";
    public static final String PROPERTY_PRINT_OUTPUT_DATA = "print.output.data";
    public static final String PROPERTY_PROXY_GRPC_INTER_NEGOTIATIONTYPE = "proxy.grpc.inter.negotiationType";
    public static final String PROPERTY_PROXY_GRPC_INTER_CA_FILE = "proxy.grpc.inter.CA.file";
    public static final String PROPERTY_PROXY_GRPC_INTER_CLIENT_CERTCHAIN_FILE = "proxy.grpc.inter.client.certChain.file";
    public static final String PROPERTY_PROXY_GRPC_INTER_CLIENT_PRIVATEKEY_FILE = "proxy.grpc.inter.client.privateKey.file";
    public static final String PROPERTY_PROXY_GRPC_INTER_SERVER_CERTCHAIN_FILE = "proxy.grpc.inter.server.certChain.file";
    public static final String PROPERTY_PROXY_GRPC_INTER_SERVER_PRIVATEKEY_FILE = "proxy.grpc.inter.server.privateKey.file";
    public static final String CURRENT_VERSION = "currentVersion";

    public static final String PROPERTY_COORDINATOR = "coordinator";
    public static final String PROPERTY_SERVER_PORT = "server.port";
    public static final String PROPERTY_INFERENCE_SERVICE_NAME = "inference.service.name";
    public static final String PROPERTY_ROUTE_TYPE = "routeType";
    public static final String PROPERTY_ROUTE_TABLE = "route.table";
    public static final String PROPERTY_AUTH_FILE = "auth.file";
    public static final String PROPERTY_AUTH_OPEN = "auth.open";
    public static final String PROPERTY_PROXY_GRPC_INTRA_PORT = "proxy.grpc.intra.port";
    public static final String PROPERTY_PROXY_GRPC_INTER_PORT = "proxy.grpc.inter.port";
    public static final String PROPERTY_PROXY_GRPC_INFERENCE_TIMEOUT = "proxy.grpc.inference.timeout";
    public static final String PROPERTY_PROXY_GRPC_INFERENCE_ASYNC_TIMEOUT = "proxy.grpc.inference.async.timeout";
    public static final String PROPERTY_PROXY_GRPC_UNARYCALL_TIMEOUT = "proxy.grpc.unaryCall.timeout";
    public static final String PROPERTY_PROXY_GRPC_THREADPOOL_CORESIZE = "proxy.grpc.threadpool.coresize";
    public static final String PROPERTY_PROXY_GRPC_THREADPOOL_MAXSIZE = "proxy.grpc.threadpool.maxsize";
    public static final String PROPERTY_PROXY_GRPC_THREADPOOL_QUEUESIZE = "proxy.grpc.threadpool.queuesize";
    public static final String PROPERTY_PROXY_ASYNC_TIMEOUT = "proxy.async.timeout";
    public static final String PROPERTY_PROXY_ASYNC_CORESIZE = "proxy.async.coresize";
    public static final String PROPERTY_PROXY_ASYNC_MAXSIZE = "proxy.async.maxsize";
    public static final String PROPERTY_PROXY_GRPC_BATCH_INFERENCE_TIMEOUT = "proxy.grpc.batch.inference.timeout";
    public static final String PROPERTY_MODEL_CACHE_PATH = "model.cache.path";
    public static final String PROPERTY_LR_USE_PARALLEL="lr.use.parallel";
    public static final String PROPERTY_ALLOW_HEALTH_CHECK = "health.check.allow";

    public static final String ACTION_TYPE_ASYNC_EXECUTE = "ASYNC_EXECUTE";

    public static final String RET_CODE = "retcode";
    public static final String RET_MSG = "retmsg";
    public static final String DATA = "data";
    public static final String STATUS = "status";
    public static final String SUCCESS = "success";
    public static final String PROB = "prob";
    public static final String ACCESS = "access";
    public static final String MODEL_WRIGHT_HIT_RATE = "modelWrightHitRate";
    public static final String INPUT_DATA_HIT_RATE = "inputDataHitRate";
    public static final String MODELING_FEATURE_HIT_RATE = "modelingFeatureHitRate";
    public static final String GUEST_MODEL_WEIGHT_HIT_RATE = "guestModelWeightHitRate";
    public static final String GUEST_INPUT_DATA_HIT_RATE = "guestInputDataHitRate";
    public static final String TAG_INPUT_FORMAT = "tag";
    public static final String SPARSE_INPUT_FORMAT = "sparse";
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
    public static final String ID = "id";
    public static final String DEVICE_ID = "device_id";
    public static final String PHONE_NUM = "phone_num";
    public static final String FEDERATED_PARAMS = "federatedParams";
    public static final String COMMIT_ID = "commitId";
    public static final String BRANCH_MASTER = "master";
    public static final String INFERENCE_AUDIT = "inferenceAudit";
    public static final String INFERENCE = "inference";
    public static final String INFERENCE_REQUEST = "inferenceRequest";
    public static final String INFERENCE_RESULT = "inferenceResult";
    public static final String FM_CROSS = "fm_cross";

    public static final String CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_TYPE_JSON_UTF8 = "application/json;charset=UTF-8";
    public static final String CHARSET_UTF8 = "UTF-8";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    public static final String TAG = "tag";
    public static final String INPUT_DATA = "input_data";
    public static final String OUTPUT_DATA = "output_data";
    public static final String COMPONENT_NAME = "componentName";
    public static final String TREE_COMPUTE_ROUND = "treeComputeRound";
    public static final String SERVICE_NAME = "serviceName";
    public static final String CALL_NAME = "callName";
    public static final String TREE_LOCATION = "treeLocation";

    public static final String UNARYCALL = "unaryCall";

    public static final String GUEST_APP_ID = "guestAppId";
    public static final String HOST_APP_ID = "hostAppId";
    public static final String SERVICE_ID = "serviceId";
    public static final String APPLY_ID = "applyId";
    public static final String FUTURE = "future";
    public static final String AUTH_FILE = "authFile";
    public static final String ENCRYPT_TYPE = "encrypt_type";


    public static final String MD5_SALT = "$1$ML";
    public static final String USER_CACHE_KEY_PREFIX = "admin_user_";

    public static final String CASE_ID = "caseid";
    public static final String CODE = "code";
    public static final String MESSAGE = "message";
    public static final String MODEL_ID = "modelId";
    public static final String MODEL_VERSION = "modelVersion";
    public static final String TIMESTAMP = "timestamp";
    public static final String APP_ID = "appid";
    public static final String PARTY_ID = "partyId";
    public static final String ROLE = "role";
    public static final String PART_ID = "partId";
    public static final String FEATURE_DATA = "featureData";
    public static final String SESSION_TOKEN = "sessionToken";

    public static final String DEFAULT_VERSION = "1.0";
    public static final String SELF_PROJECT_NAME = "proxy";
    public static final String SELF_ENVIRONMENT = "online";
    public static final String HEAD = "head";
    public static final String BODY = "body";
    public static final String SERVICENAME_INFERENCE = "inference";
    public static final String SERVICENAME_BATCH_INFERENCE = "batchInference";
    public static final String SERVICENAME_START_INFERENCE_JOB = "startInferenceJob";
    public static final String SERVICENAME_GET_INFERENCE_RESULT = "getInferenceResult";

    // event
    public static final String EVENT_INFERENCE = "inference";
    public static final String EVENT_UNARYCALL = "unaryCall";
    public static final String EVENT_ERROR = "error";
    public static final String EVENT_SET_INFERENCE_CACHE = "setInferenceCache";
    public static final String EVENT_SET_BATCH_INFERENCE_CACHE = "setBatchInferenceCache";

    public static final String LOCAL_INFERENCE_DATA = "localInferenceData";
    public static final String REMOTE_INFERENCE_DATA = "remoteInferenceData";

    public static final String SBT_TREE_NODE_ID_ARRAY = "sbtTreeNodeIdArray";

    public static final String REMOTE_METHOD_BATCH = "batch";
    public static final String MODEL_NAME_SPACE = "modelNameSpace";
    public static final String MODEL_TABLE_NAME = "modelTableName";
    public static final String REGISTER_ENVIRONMENT = "online";
    public static final String SERVICE_SERVING = "serving";
    public static final String SERVICE_PROXY = "proxy";
    public static final String SERVICE_ADMIN = "admin";
    public static final String FAILED = "failed";
    public static final String BATCH_PRC_TIMEOUT = "batch.rpc.timeout";
    public static final String PASS_QPS = "passQps";
    // parameters
    public static final String PARAMS_INITIATOR = "initiator";
    public static final String PARAMS_ROLE = "role";
    public static final String PARAMS_JOB_PARAMETERS = "job_parameters";
    public static final String PARAMS_SERVICE_ID = "service_id";
    public static final String BATCH_INFERENCE_SPLIT_SIZE = "batch.inference.split.size";
    public static final String WARN_LIST = "warnList";
    public static final String ERROR_LIST =  "errorList";
    public static final String HEALTH_INFO =  "healthInfo";
    public static final String PROPERTY_ADMIN_HEALTH_CHECK_TIME = "health.check.time";

    public static final String HTTP_CLIENT_CONFIG_CONN_REQ_TIME_OUT = "httpclinet.config.connection.req.timeout";
    public static final String HTTP_CLIENT_CONFIG_CONN_TIME_OUT = "httpclient.config.connection.timeout";
    public static final String HTTP_CLIENT_CONFIG_SOCK_TIME_OUT = "httpclient.config.sockect.timeout";
    public static final String HTTP_CLIENT_INIT_POOL_MAX_TOTAL = "httpclient.init.pool.maxtotal";
    public static final String HTTP_CLIENT_INIT_POOL_DEF_MAX_PER_ROUTE = "httpclient.init.pool.def.max.pre.route";
    public static final String HTTP_CLIENT_INIT_POOL_SOCK_TIME_OUT = "httpclient.init.pool.sockect.timeout";
    public static final String HTTP_CLIENT_INIT_POOL_CONN_TIME_OUT = "httpclient.init.pool.connection.timeout";
    public static final String HTTP_CLIENT_INIT_POOL_CONN_REQ_TIME_OUT = "httpclient.init.pool.connection.req.timeout";
    public static final String HTTP_CLIENT_TRAN_CONN_REQ_TIME_OUT = "httpclient.tran.connection.req.timeout";
    public static final String HTTP_CLIENT_TRAN_CONN_TIME_OUT = "httpclient.tran.connection.timeout";
    public static final String HTTP_CLIENT_TRAN_SOCK_TIME_OUT = "httpclient.tran.sockect.timeout";

    public static final String PROPERTY_HTTP_CONNECT_TIMEOUT = "http.connect.timeout";
    public static final String PROPERTY_HTTP_SOCKET_TIMEOUT = "http.socket.timeout";
    public static final String PROPERTY_HTTP_MAX_POOL_SIZE = "http.max.pool.size";
    public static final String PROPERTY_HTTP_CONNECT_REQUEST_TIMEOUT = "http.connect.request.timeout";
    public static final String PROPERTY_HTTP_ADAPTER_URL = "http.adapter.url";
}
