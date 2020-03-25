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
    public   static  final String  GUEST_PARAM_ERROR  ="1100";
    public   static  final String  HOST_PARAM_ERROR  ="2100";
    public   static  final String  GUEST_FEATURE_ERROR  ="1102";
    public   static  final String  HOST_FEATURE_ERROR  ="2102";
    public   static  final String  HOST_FEATURE_NOT_EXIST  ="2113";
    public   static  final String  GUEST_MODEL_NULL  ="1104";
    public   static  final String  HOST_MODEL_NULL  ="2104";
    public   static  final String  GUEST_NET_ERROR  ="1105";
    public   static  final String  GUEST_LOAD_MODEL_ERROR  ="1107";
    public   static  final String  HOST_LOAD_MODEL_ERROR  ="2107";
    public   static   final  String   GUEST_MERGE_ERROR =  "1108";

    public   static   final  String  HOST_NOT_SUPPORT_ERROR = "2108";





}
