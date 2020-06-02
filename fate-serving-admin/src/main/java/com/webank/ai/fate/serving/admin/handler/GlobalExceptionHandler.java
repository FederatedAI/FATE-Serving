package com.webank.ai.fate.serving.admin.handler;

import com.webank.ai.fate.serving.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.exceptions.RemoteRpcException;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * @Description Global exceptions management
 * @Date: 2020/3/25 11:11
 * @Author: v_dylanxu
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(value = {MethodArgumentTypeMismatchException.class, IllegalArgumentException.class})
    public ReturnResult paramValidationExceptionHandle(Exception e) {
        logger.error("[ParamValidationException]Exception:", e);
        return ReturnResult.build(StatusCode.PARAM_ERROR, e.getMessage());
    }

    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(value = {StatusRuntimeException.class, RemoteRpcException.class})
    public ReturnResult statusRuntimeExceptionHandle(Exception e) {
        logger.error("[RemoteRpcException]Exception:", e);
        return ReturnResult.build(StatusCode.NET_ERROR, "remote rpc request timeout");
    }

    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(value = Exception.class)
    public ReturnResult commonExceptionHandle(Exception e) {
        logger.error("[SystemException]Exception:", e);
        return ReturnResult.build(StatusCode.SYSTEM_ERROR, "System Error, please try again later");
    }

}