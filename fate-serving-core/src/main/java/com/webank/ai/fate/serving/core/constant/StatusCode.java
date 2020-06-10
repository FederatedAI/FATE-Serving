package com.webank.ai.fate.serving.core.constant;

public class StatusCode {

    public static final String SUCCESS = "0";
    public static final String SYSTEM_ERROR = "500";
    public static final String NET_ERROR = "501";
    public static final String OVER_LOAD_ERROR = "502";
    public static final String SHUTDOWN_ERROR = "503";
    public static final String INVALID_ROLE_ERROR = "504";
    public static final String SERVICE_NOT_FOUND = "505";
    public static final String MODEL_INIT_ERROR = "506";
    public static final String PARAM_ERROR = "100";
    public static final String GUEST_PARAM_ERROR = "100";
    public static final String HOST_PARAM_ERROR = "100";
    public static final String GUEST_FEATURE_ERROR = "101";
    public static final String HOST_FEATURE_ERROR = "101";
    public static final String GUEST_LOAD_MODEL_ERROR = "102";
    public static final String HOST_LOAD_MODEL_ERROR = "102";
    public static final String GUEST_BIND_MODEL_ERROR = "103";
    public static final String HOST_BIND_MODEL_ERROR = "103";
    public static final String MODEL_NULL = "104";

    public static final String HOST_MODEL_NULL = "104";
    public static final String GUEST_ROUTER_ERROR = "105";
    public static final String HOST_UNSUPPORTED_COMMAND_ERROR = "106";
    public static final String GUEST_MERGE_ERROR = "107";
    public static final String HOST_FEATURE_NOT_EXIST = "108";
    public static final String FEATURE_DATA_ADAPTOR_ERROR = "109";




}
