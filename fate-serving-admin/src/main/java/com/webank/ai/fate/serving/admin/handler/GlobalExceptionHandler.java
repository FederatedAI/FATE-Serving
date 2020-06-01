package com.webank.ai.fate.serving.admin.handler;

import com.webank.ai.fate.serving.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.exceptions.RemoteRpcException;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * @Description Global exceptions management
 * @Date: 2020/3/25 11:11
 * @Author: v_dylanxu
 */
@ControllerAdvice(annotations = ResponseBody.class)
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = {MethodArgumentTypeMismatchException.class, IllegalArgumentException.class})
    public ReturnResult paramValidationExceptionHandle(Exception e) {
        logger.error("[ParamValidationException]Exception:", e);
        ReturnResult result = new ReturnResult();
        result.setRetcode(StatusCode.PARAM_ERROR);
        result.setRetmsg(e.getMessage());
        return result;
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
    @ExceptionHandler(value = {StatusRuntimeException.class, RemoteRpcException.class})
    public ReturnResult statusRuntimeExceptionHandle(Exception e) {
        logger.error("[RemoteRpcException]Exception:", e);
        ReturnResult result = new ReturnResult();
        result.setRetcode(StatusCode.NET_ERROR);
        result.setRetmsg("remote rpc request timeout");
        return result;
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = Exception.class)
    public ReturnResult commonExceptionHandle(Exception e) {
        logger.error("[SystemException]Exception:", e);
        ReturnResult result = new ReturnResult();
        result.setRetcode(StatusCode.SYSTEM_ERROR);
        result.setRetmsg("System Error, please try again later! Message:" + e.getMessage());
        return result;
    }

}