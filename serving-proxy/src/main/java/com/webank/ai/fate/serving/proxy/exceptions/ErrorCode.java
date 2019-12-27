package com.webank.ai.fate.serving.proxy.exceptions;

public class ErrorCode {

    public  static  String  PARAM_ERROR ="100";  //参数错误
    public  static  String  ROLE_ERROR ="101";   //鉴权错误
    public  static  String  SERVICE_NOT_FOUND= "102"; //服务不存在
    public  static  String  SYSTEM_ERROR = "103";//系统错误
    public  static  String  LIMIT_ERROR="104";// 系统限流
    public  static  String  QUOTA_ERROR="105";// 配额耗尽
    public  static  String  ORDER_ERROR="106";// 订单信息异常
    public  static  String  NET_ERROR="107";// 网络异常
    public  static  String  SHUTDOWN_ERROR="108";// 服务器关闭异常
    public  static  String  ROUTER_ERROR="109";// 路由信息异常





}
