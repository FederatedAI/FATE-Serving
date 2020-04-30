package com.webank.ai.fate.serving.core.rpc.core;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;



/**
 * @Description TODO
 * @Author
 **/
public class ErrorMessageUtil {

   static Logger logger = LoggerFactory.getLogger(ErrorMessageUtil.class);


    public static ReturnResult  handleExceptionToReturnResult(Throwable e){
        ReturnResult  returnResult = new  ReturnResult();
        if(e instanceof BaseException){
            BaseException baseException  = (BaseException) e;
            returnResult.setRetcode(baseException.getRetcode());
            returnResult.setRetmsg(e.getMessage());
        }else{
            returnResult.setRetcode(ErrorCode.SYSTEM_ERROR);
        }
        return  returnResult;

    }
    public static String  buildRemoteRpcErrorMsg(String  code ,String  msg){
        return  new StringBuilder().append("host return code ").append(code)
                .append(" host msg :").append(msg).toString();

    }

    public static String  transformRemoteErrorCode(String  code ){
        if(code!=null) {
            return new StringBuilder().append("2").append(code).toString();
        }
        else{
            return new StringBuilder().append("2").append(StatusCode.SYSTEM_ERROR).toString();
        }
    }




    public static Map  handleExceptionToMap(Throwable e){
        Map  returnResult = new HashMap();
        if(e instanceof BaseException){
            BaseException baseException  = (BaseException) e;
            returnResult.put(Dict.RET_CODE,baseException.getRetcode());
            returnResult.put(Dict.MESSAGE,baseException.getMessage());
        }else{
            returnResult.put(Dict.RET_CODE,ErrorCode.SYSTEM_ERROR);
        }
        return  returnResult;
    }



   public static  Map handleException(Map result,Throwable e){

       if (e instanceof IllegalArgumentException) {
           result.put(Dict.CODE, ErrorCode.PARAM_ERROR);
           result.put(Dict.MESSAGE,"PARAM_ERROR");
       }
       else if(e instanceof NoRouteInfoException){
           result.put(Dict.CODE, ErrorCode.ROUTER_ERROR);
           result.put(Dict.MESSAGE, "ROUTER_ERROR");
       } else if (e instanceof SysException) {
           result.put(Dict.CODE, ErrorCode.SYSTEM_ERROR);
           result.put(Dict.MESSAGE, "SYSTEM_ERROR");


       } else if (e instanceof BlockException) {
           result.put(Dict.CODE, ErrorCode.LIMIT_ERROR);

           result.put(Dict.MESSAGE, "OVERLOAD");

       } else if (e instanceof InvalidRoleInfoException) {
           result.put(Dict.CODE, ErrorCode.ROLE_ERROR);
           result.put(Dict.MESSAGE, "ROLE_ERROR");
       }  else if (e instanceof ShowDownRejectException){
           result.put(Dict.CODE, ErrorCode.SHUTDOWN_ERROR);
           result.put(Dict.MESSAGE, "SHUTDOWN_ERROR");

       }
       else if (e instanceof NoResultException) {
           logger.error("NET_ERROR ",e);
           result.put(Dict.CODE, ErrorCode.NET_ERROR);
           result.put(Dict.MESSAGE, "NET_ERROR");
       } else {
           logger.error("SYSTEM_ERROR ",e);
           result.put(Dict.CODE, ErrorCode.SYSTEM_ERROR);
           result.put(Dict.MESSAGE, "SYSTEM_ERROR");
       }

       return  result;

   }
}
