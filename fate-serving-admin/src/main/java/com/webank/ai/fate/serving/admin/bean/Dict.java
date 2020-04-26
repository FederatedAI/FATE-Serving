package com.webank.ai.fate.serving.admin.bean;

/**
 * @Description
 * @Date: 2020/3/25 15:22
 * @Author: v_dylanxu
 */
public class Dict {

    public static final String CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_TYPE_JSON_UTF8 = "application/json;charset=UTF-8";
    public static final String CHARSET_UTF8 = "UTF-8";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    public static final String RETCODE = "retcode";
    public static final String MESSAGE = "message";
    public static final String CODE = "code";
    public static final String DATA = "data";
    public static final String HOST = "host";
    public static final String PORT = "port";

    public static final String HEAD = "head";
    public static final String BODY = "body";

    public static final String REGISTER_PROJECT = "admin";
    public static final String REGISTER_ENVIRONMENT = "online";

    public static final String SERVICE_SERVING = "serving";
    public static final String SERVICE_PROXY = "proxy";

    public static final String SUCCESS = "success";
    public static final String FAILED = "failed";

    public static final String USER_CACHE_KEY_PREFIX = "admin_user_";
    public static final String MD5_SALT = "$1$FSA";

    // parameters
    public static final String INITIATOR = "initiator";
    public static final String ROLE = "role";
    public static final String JOB_PARAMETERS = "job_parameters";
    public static final String SERVICE_ID = "service_id";
    public static final String TABLE_NAME = "tableName";
    public static final String NAMESPACE = "namespace";

    public static final String SERVICE_NAME_MODEL_QUERY = "modelQuery";

    public static final String LIST_ALL_MODEL = "listAllModel";
    public static final String GET_MODEL_BY_NAME_AND_NAMESPACE = "getModelByNameAndNamespace";
    public static final String GET_MODEL_BY_SERVICE_ID = "getModelByServiceId";
    public static final String PUBLISH_LOAD = "publishLoad";
    public static final String PUBLISH_BIND = "publishBind";
    public static final String UNLOAD = "unload";
    public static final String UNBIND = "unbind";


}
