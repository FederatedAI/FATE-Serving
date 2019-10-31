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

package com.webank.ai.fate.register.common;


import java.util.regex.Pattern;

public interface Constants {

    public static String ZOOKEEPER_REGISTER = "zookeeper_register";

    public static String PARTY_ID = "party_id";

    public static String ROUTER_MODEL = "ROUTER_MODEL";

    public static String VERSION = "version";

    public static String RETRY_PERID_KEY = "retry_period_key";

    public static String REGISTRY_FILESAVE_SYNC_KEY = "registry_filesave_sync_key";

    public static String FILE_KEY = "file";

    public static String APPLICATION_KEY = "application";

    public static String BACKUP_KEY = "backup";

    public static String PROTOCOL_KEY = "protocol";


    public static String PROJECT_KEY = "project";

    public static String ENVIRONMENT_KEY = "environment";

    public static String SERVER_PORT = "server_port";


    public static String HOST_KEY = "host";

    public static String PORT_KEY = "port";

    public static String PATH_KEY = "path";

    public static String PATH_JMX = "jmx";

    public static String JMX_PROTOCOL_KEY = "service:jmx:rmi:///jndi/rmi";

    String PROVIDER = "com/webank/ai/fate/register/provider";

    String CONSUMER = "consumer";


    String CHECK_KEY = "check";


    String REMOTE_APPLICATION_KEY = "remote.application";

    String ENABLED_KEY = "enabled";

    String DISABLED_KEY = "disabled";


    String ENVIRONMENT = "environment";

    String ANY_VALUE = "*";

    String COMMA_SEPARATOR = ",";

    String DOT_SEPARATOR = ".";

    Pattern COMMA_SPLIT_PATTERN = Pattern.compile("\\s*[,]+\\s*");

    String PATH_SEPARATOR = "/";

    String PROTOCOL_SEPARATOR = "://";

    String REGISTRY_SEPARATOR = "|";

    Pattern REGISTRY_SPLIT_PATTERN = Pattern.compile("\\s*[|;]+\\s*");

    String SEMICOLON_SEPARATOR = ";";

    Pattern SEMICOLON_SPLIT_PATTERN = Pattern.compile("\\s*[;]+\\s*");

    Pattern EQUAL_SPLIT_PATTERN = Pattern.compile("\\s*[=]+\\s*");

    String DEFAULT_PROXY = "javassist";


    int DEFAULT_CORE_THREADS = 0;

    int DEFAULT_THREADS = 200;

    String THREADPOOL_KEY = "threadpool";

    String THREAD_NAME_KEY = "threadname";

    String CORE_THREADS_KEY = "corethreads";

    String THREADS_KEY = "threads";

    String QUEUES_KEY = "queues";

    String ALIVE_KEY = "alive";

    String DEFAULT_THREADPOOL = "limited";

    String DEFAULT_CLIENT_THREADPOOL = "cached";

    String IO_THREADS_KEY = "iothreads";

    int DEFAULT_QUEUES = 0;

    int DEFAULT_ALIVE = 60 * 1000;

    String TIMEOUT_KEY = "timeout";

    int DEFAULT_TIMEOUT = 1000;

    String REMOVE_VALUE_PREFIX = "-";

    String PROPERTIES_CHAR_SEPERATOR = "-";

    String UNDERLINE_SEPARATOR = "_";

    String SEPARATOR_REGEX = "_|-";

    String GROUP_CHAR_SEPERATOR = ":";

    String HIDE_KEY_PREFIX = ".";

    String DOT_REGEX = "\\.";

    String DEFAULT_KEY_PREFIX = "default.";

    String DEFAULT_KEY = "default";

    /**
     * Default timeout value in milliseconds for server shutdown
     */
    int DEFAULT_SERVER_SHUTDOWN_TIMEOUT = 10000;

    String SIDE_KEY = "side";

    String PROVIDER_SIDE = "com/webank/ai/fate/register/provider";

    String CONSUMER_SIDE = "consumer";

    String ANYHOST_KEY = "anyhost";

    String ANYHOST_VALUE = "0.0.0.0";

    String LOCALHOST_KEY = "localhost";

    String LOCALHOST_VALUE = "127.0.0.1";

    String METHODS_KEY = "methods";

    String METHOD_KEY = "method";

    String PID_KEY = "pid";

    String TIMESTAMP_KEY = "timestamp";

    String GROUP_KEY = "group";

    String INTERFACE_KEY = "interface";


    String DUMP_DIRECTORY = "dump.directory";

    String CLASSIFIER_KEY = "classifier";

    String VERSION_KEY = "version";

    String REVISION_KEY = "revision";

    /**
     * package version in the manifest
     */
    String RELEASE_KEY = "release";

    int MAX_PROXY_COUNT = 65535;

    String MONITOR_KEY = "monitor";
    String CLUSTER_KEY = "cluster";


    String REGISTRY_KEY = "registry";

    String REGISTRY_PROTOCOL = "registry";

    String DYNAMIC_KEY = "dynamic";

    String CATEGORY_KEY = "category";

    String PROVIDERS_CATEGORY = "providers";

    String CONSUMERS_CATEGORY = "consumers";

    String ROUTERS_CATEGORY = "routers";

    String DYNAMIC_ROUTERS_CATEGORY = "dynamicrouters";

    String DEFAULT_CATEGORY = PROVIDERS_CATEGORY;

    String CONFIGURATORS_CATEGORY = "configurators";

    String DYNAMIC_CONFIGURATORS_CATEGORY = "dynamicconfigurators";

    String APP_DYNAMIC_CONFIGURATORS_CATEGORY = "appdynamicconfigurators";

    String ROUTERS_SUFFIX = ".routers";

    String EMPTY_PROTOCOL = "empty";

    String ROUTE_PROTOCOL = "route";

    String OVERRIDE_PROTOCOL = "override";

    String WEIGHT_KEY = "weight";

    String COMPATIBLE_CONFIG_KEY = "compatible_config";


    String REGISTER_IP_KEY = "register.ip";

    String REGISTER_KEY = "register";

    String SUBSCRIBE_KEY = "subscribe";

    String DEFAULT_REGISTRY = "fate";

    String REGISTER = "register";

    String UNREGISTER = "unregister";

    String SUBSCRIBE = "subscribe";

    String UNSUBSCRIBE = "unsubscribe";

    String CONFIGURATORS_SUFFIX = ".configurators";

    String ADMIN_PROTOCOL = "admin";

    String PROVIDER_PROTOCOL = "com/webank/ai/fate/register/provider";

    String CONSUMER_PROTOCOL = "consumer";

    String SCRIPT_PROTOCOL = "script";

    String CONDITION_PROTOCOL = "condition";
    String TRACE_PROTOCOL = "trace";

    String SIMPLIFIED_KEY = "simplified";


    String EXTRA_KEYS_KEY = "extra-keys";

    String REGISTRY_RECONNECT_PERIOD_KEY = "reconnect.period";

    int DEFAULT_SESSION_TIMEOUT = 60 * 1000;


    int DEFAULT_REGISTRY_RETRY_TIMES = 3;

    int DEFAULT_REGISTRY_RECONNECT_PERIOD = 3 * 1000;


    int DEFAULT_REGISTRY_RETRY_PERIOD = 5 * 1000;


    String REGISTRY_RETRY_TIMES_KEY = "retry.times";


    String REGISTRY_RETRY_PERIOD_KEY = "retry.period";

    String SESSION_TIMEOUT_KEY = "session";

    String UTF_8 = "UTF-8";
    String GRPC = "grpc";
    String POOL_PREFIX = "pool-";
    String POOL_PREFIX_THREAD = "-thread-";
    String REFIX_FATE_REGISTRY_RETRY_TIMER = "FateRegistryRetryTimer";
    String OS_NAME = "os.name";
    String USER_HOME = "user.home";
    String OS_NAME_WIN = "win";

}
