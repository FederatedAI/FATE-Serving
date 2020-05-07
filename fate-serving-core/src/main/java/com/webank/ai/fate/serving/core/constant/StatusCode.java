package com.webank.ai.fate.serving.core.constant;

public class StatusCode {

    public   static  final String  SUCCESS  ="0";
    public   static  final String  GUEST_PARAM_ERROR  ="100";
    public   static  final String  HOST_PARAM_ERROR  ="100";
    public   static  final String  GUEST_FEATURE_ERROR ="102";
    public   static  final String  HOST_FEATURE_ERROR  ="102";
    public   static  final String  HOST_FEATURE_NOT_EXIST  ="113";
    public   static  final String  MODEL_NULL  ="104";
    public   static  final String  HOST_MODEL_NULL   ="104";
    public   static  final String  GUEST_ROUTER_ERROR = "106";
    public   static  final String  NET_ERROR  ="105";
    public   static  final String  GUEST_LOAD_MODEL_ERROR  ="107";
    public   static  final String  HOST_LOAD_MODEL_ERROR  ="107";
    public   static  final String  GUEST_MERGE_ERROR =  "108";
    public   static  final String  GUEST_BIND_MODEL_ERROR =  "109";
    public   static  final String  HOST_BIND_MODEL_ERROR =  "109";
    public   static  final String  SYSTEM_ERROR = "110";
    public   static  final String  SHUTDOWN_ERROR = "111";
    public   static  final String  HOST_NOT_SUPPORT_ERROR = "115";
    public   static  final String  PARAM_ERROR = "120";
    public   static  final String  UNAUTHORIZED = "121";


}
