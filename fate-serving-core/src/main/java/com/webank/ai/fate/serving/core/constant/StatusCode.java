package com.webank.ai.fate.serving.core.constant;

public class StatusCode {


//     *    guest 参数错误   1100   异常  GuestInvalidParamException
// *    host  参数错误   2100   异常  HostInvalidParamException
// *    guest 特征错误   1102   异常  GuestInvalidFeatureException
// *    host  特征错误   2102   异常  HostInvalidFeatureException
// *    host  特征不存在  2113  异常  HostNoFeatureException
// *    guest 模型不存在 1104   异常  GuestModelNullException
// *    host  模型不存在 2104   异常  HostModelNullException
// *    guest 通信异常   1105   异常  GuestNetErrorExcetpion
// *    guest 通讯路由不存在  4115 异常  NoRouteInfoException
// *    guest  host返回数据异常  1115  HostReturnErrorException
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
    public   static  final String  SYSTEM_ERROR = "110";
    public   static  final String  HOST_NOT_SUPPORT_ERROR = "115";


//    public static final String OK = "0";
//    public static final int EMPTY_DATA = 100;
//    public static final int NUMERICAL_ERROR = 101;
//    public static final int INVALID_FEATURE = 102;
//    public static final int GET_FEATURE_FAILED = 103;
//    public static final int LOAD_MODEL_FAILED = 104;
//    public static final int NETWORK_ERROR = 105;
//    public static final int DISK_ERROR = 106;
//    public static final int STORAGE_ERROR = 107;
//    public static final int COMPUTE_ERROR = 108;
//    public static final int NO_RESULT = 109;
//    //public static final int SYSTEM_ERROR = 110;
//    public static final int ADAPTER_ERROR = 111;
//    public static final int DEAL_FEATURE_FAILED = 112;
//    public static final int NO_FEATURE = 113;




}
