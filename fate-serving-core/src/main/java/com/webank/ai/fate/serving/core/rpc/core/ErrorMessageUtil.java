package com.webank.ai.fate.serving.core.rpc.core;

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


    public static ReturnResult handleExceptionToReturnResult(Throwable e) {
        ReturnResult returnResult = new ReturnResult();
        if (e instanceof BaseException) {
            BaseException baseException = (BaseException) e;
            returnResult.setRetcode(baseException.getRetcode());
            returnResult.setRetmsg(e.getMessage());
        } else {
            returnResult.setRetcode(StatusCode.SYSTEM_ERROR);
        }
        return returnResult;

    }

    public static String buildRemoteRpcErrorMsg(String code, String msg) {
        return new StringBuilder().append("host return code ").append(code)
                .append(" host msg :").append(msg).toString();

    }

    public static String transformRemoteErrorCode(String code) {
        if (code != null) {
            return new StringBuilder().append("2").append(code).toString();
        } else {
            return new StringBuilder().append("2").append(StatusCode.SYSTEM_ERROR).toString();
        }
    }


    public static String getLocalExceptionCode(Exception e) {
        String retcode = StatusCode.SYSTEM_ERROR;
        if (e instanceof BaseException) {
            retcode = ((BaseException) e).getRetcode();
        }

        return retcode;
    }


    public static AbstractServiceAdaptor.ExceptionInfo handleExceptionExceptionInfo(Throwable e) {
        AbstractServiceAdaptor.ExceptionInfo exceptionInfo  = new AbstractServiceAdaptor.ExceptionInfo();
        if (e instanceof BaseException) {
            BaseException baseException = (BaseException) e;
            exceptionInfo.setCode( baseException.getRetcode());
            exceptionInfo.setMessage(baseException.getMessage());
        } else {
            exceptionInfo.setCode(StatusCode.SYSTEM_ERROR);
        }
        return exceptionInfo;
    }


    public static Map handleExceptionToMap(Throwable e) {
        Map returnResult = new HashMap();
        if (e instanceof BaseException) {
            BaseException baseException = (BaseException) e;
            returnResult.put(Dict.RET_CODE, baseException.getRetcode());
            returnResult.put(Dict.MESSAGE, baseException.getMessage());
        } else {
            returnResult.put(Dict.RET_CODE, StatusCode.SYSTEM_ERROR);
        }
        return returnResult;
    }


    public static Map handleException(Map result, Throwable e) {

        if (e instanceof IllegalArgumentException) {
            result.put(Dict.CODE, StatusCode.PARAM_ERROR);
            result.put(Dict.MESSAGE, "PARAM_ERROR");
        } else if (e instanceof NoRouteInfoException) {
            result.put(Dict.CODE, StatusCode.GUEST_ROUTER_ERROR);
            result.put(Dict.MESSAGE, "ROUTER_ERROR");
        } else if (e instanceof SysException) {
            result.put(Dict.CODE, StatusCode.SYSTEM_ERROR);
            result.put(Dict.MESSAGE, "SYSTEM_ERROR");
        }
        else if (e instanceof OverLoadException) {
            result.put(Dict.CODE, StatusCode.OVER_LOAD_ERROR);
            result.put(Dict.MESSAGE, "OVER_LOAD");
        }
        else if (e instanceof InvalidRoleInfoException) {
            result.put(Dict.CODE, StatusCode.INVALID_ROLE_ERROR);
            result.put(Dict.MESSAGE, "ROLE_ERROR");
        } else if (e instanceof ShowDownRejectException) {
            result.put(Dict.CODE, StatusCode.SHUTDOWN_ERROR);
            result.put(Dict.MESSAGE, "SHUTDOWN_ERROR");

        } else if (e instanceof NoResultException) {
            logger.error("NET_ERROR ", e);
            result.put(Dict.CODE, StatusCode.NET_ERROR);
            result.put(Dict.MESSAGE, "NET_ERROR");
        } else {
            logger.error("SYSTEM_ERROR ", e);
            result.put(Dict.CODE, StatusCode.SYSTEM_ERROR);
            result.put(Dict.MESSAGE, "SYSTEM_ERROR");
        }

        return result;

    }
}
