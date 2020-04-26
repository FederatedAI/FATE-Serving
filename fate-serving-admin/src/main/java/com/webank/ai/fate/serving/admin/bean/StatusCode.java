package com.webank.ai.fate.serving.admin.bean;

public class StatusCode {

    public static final int SUCCESS = 0;
    public static final int SYSTEM_ERROR = 1001;
    public static final int PARAM_ERROR = 1002;
    public static final int NET_ERROR = 1003;
    public static final int USER_ERROR = 1004;
    public static final int LOAD_MODEL_ERROR = 1005;
    public static final int BIND_MODEL_ERROR = 1006;
    public static final int SERVICE_NOT_FOUND = 1007;
    public static final int SHUTDOWN_ERROR = 1008;
    public static final int UNAVAILABLE_REQUEST = 1009;

}
