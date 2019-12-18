package com.webank.ai.fate.serving.proxy.common;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.webank.ai.fate.serving.proxy.exceptions.*;
import com.webank.ai.fate.serving.proxy.exceptions.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.webank.ai.fate.serving.proxy.common.Dict.CODE;
import static com.webank.ai.fate.serving.proxy.common.Dict.MESSAGE;

/**
 * @Description TODO
 * @Author
 **/
public class ErrorMessageUtil {

   static Logger logger = LoggerFactory.getLogger(ErrorMessageUtil.class);

   public static  Map handleException(Map result,Throwable e){

       if (e instanceof IllegalArgumentException) {
           /**
            * 参数错误
            *
            */
           result.put(CODE, com.webank.ai.fate.serving.proxy.exceptions.ErrorCode.PARAM_ERROR);
           result.put(MESSAGE,"PARAM_ERROR");
       }else if ( e instanceof SqlAttactException)
       {
           result.put(CODE, com.webank.ai.fate.serving.proxy.exceptions.ErrorCode.PARAM_ERROR);
           result.put(MESSAGE,"SqlAttactException");
       }

       else if(e instanceof NoRouteInfoException){

           /**
            * 无路由信息
            */

           result.put(CODE, com.webank.ai.fate.serving.proxy.exceptions.ErrorCode.ROUTER_ERROR);
           result.put(MESSAGE, "ROUTER_ERROR");


       } else if (e instanceof SysException) {
           /**
            * 系统错误
            */
           result.put(CODE, com.webank.ai.fate.serving.proxy.exceptions.ErrorCode.SYSTEM_ERROR);
           result.put(MESSAGE, "SYSTEM_ERROR");


       } else if (e instanceof BlockException) {
           /**
            * 系统限流
            */
           result.put(CODE, com.webank.ai.fate.serving.proxy.exceptions.ErrorCode.LIMIT_ERROR);

           result.put(MESSAGE, "OVERLOAD");

       } else if (e instanceof InvalidRoleInfoException) {
           /**
            *  鉴权错误
            */
           result.put(CODE, com.webank.ai.fate.serving.proxy.exceptions.ErrorCode.ROLE_ERROR);
           result.put(MESSAGE, "ROLE_ERROR");
       }  else if (e instanceof   ShowDownRejectException){

           /**
            *  关闭拒绝
            */
           result.put(CODE, com.webank.ai.fate.serving.proxy.exceptions.ErrorCode.SHUTDOWN_ERROR);
           result.put(MESSAGE, "SHUTDOWN_ERROR");

       }
       else if (e instanceof NoResultException) {
           logger.error("NET_ERROR ",e);
           /**
            * 网络异常
            */
           result.put(CODE, com.webank.ai.fate.serving.proxy.exceptions.ErrorCode.NET_ERROR);
           result.put(MESSAGE, "NET_ERROR");
       } else {
           /**
            * 系统异常
            */
           logger.error("SYSTEM_ERROR ",e);
           result.put(CODE, ErrorCode.SYSTEM_ERROR);
           result.put(MESSAGE, "SYSTEM_ERROR");
       }

       return  result;

   }
}
