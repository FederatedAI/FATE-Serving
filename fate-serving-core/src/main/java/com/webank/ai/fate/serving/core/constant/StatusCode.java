/*
 * Copyright 2019 The FATE Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.ai.fate.serving.core.constant;

public class StatusCode {

    public static final int SUCCESS = 0;
    public static final int GUEST_PARAM_ERROR = 100;
    public static final int HOST_PARAM_ERROR = 100;
    public static final int GUEST_FEATURE_ERROR = 102;
    public static final int HOST_FEATURE_ERROR = 102;
    public static final int MODEL_NULL = 104;
    public static final int HOST_MODEL_NULL = 104;
    public static final int NET_ERROR = 105;
    public static final int GUEST_ROUTER_ERROR = 106;
    public static final int GUEST_LOAD_MODEL_ERROR = 107;
    public static final int HOST_LOAD_MODEL_ERROR = 107;
    public static final int GUEST_MERGE_ERROR = 108;
    public static final int GUEST_BIND_MODEL_ERROR = 109;
    public static final int HOST_BIND_MODEL_ERROR = 109;
    public static final int SYSTEM_ERROR = 110;
    public static final int SHUTDOWN_ERROR = 111;
    public static final int HOST_FEATURE_NOT_EXIST = 113;
    public static final int FEATURE_DATA_ADAPTOR_ERROR = 116;
    public static final int PARAM_ERROR = 120;
    public static final int INVALID_ROLE_ERROR = 121;
    public static final int SERVICE_NOT_FOUND = 122;
    public static final int MODEL_INIT_ERROR = 123;
    public static final int OVER_LOAD_ERROR = 124;
    public static final int HOST_UNSUPPORTED_COMMAND_ERROR = 125;
    public static final int UNREGISTER_ERROR = 126;
    public static final int INVALID_TOKEN = 127;
    public static final int PROXY_ROUTER_ERROR = 128;
    public static final int PROXY_AUTH_ERROR = 129;
    public static final int MODEL_SYNC_ERROR = 130;
    public static final int Fill_RATE_ERROR = 131; // 填充率异常
    public static final int PROXY_LOAD_ROUTER_TABLE_ERROR = 132;
    public static final int PROXY_UPDATE_ROUTER_TABLE_ERROR = 133;
    public static final int PROXY_PARAM_ERROR = 134;
    public static final int INVALID_RESPONSE =  135;




}
