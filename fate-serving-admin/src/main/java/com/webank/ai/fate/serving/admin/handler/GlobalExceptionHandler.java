package com.webank.ai.fate.serving.admin.handler;

import com.webank.ai.fate.serving.admin.bean.ReturnResult;
import com.webank.ai.fate.serving.admin.bean.StatusCode;
import com.webank.ai.fate.serving.admin.exceptions.RemoteRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
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

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = {MethodArgumentTypeMismatchException.class, IllegalArgumentException.class})
    public ReturnResult paramValidationExceptionHandle(Exception e) {
        logger.error("[ParamValidationException]Exception:", e);
        return ReturnResult.failure(StatusCode.PARAM_ERROR, e.getMessage());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler(value = RemoteRpcException.class)
    public ReturnResult remoteRpcExceptionHandle(RemoteRpcException e) {
        logger.error("[RemoteRpcException]Exception:", e);
        return ReturnResult.failure(e.getCode(), e.getMessage());
    }

    /*@ResponseBody
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(value = {HttpRequestMethodNotSupportedException.class})
    public ReturnResult methodNotSupportExceptionHandle(Exception e) {
        logger.error("[MethodNotSupportException]Exception:", e);
        return ReturnResult.failure(HttpStatus.METHOD_NOT_ALLOWED.value(), "Method not support! Message:" + e.getMessage());
    }*/

    /*
    @ResponseBody
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(value = PermissionDeniedException.class)
    public CommonResponse permissionDeniedExceptionHandle(Exception e) {
        CommonResponse commonResponse = CommonResponse.createCommonResponse();
        logger.error("[PermissionDeniedException]Exception:", e);
        return commonResponse.fail("Permission Denied!");
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = {ParamValidationException.class,
            ServletRequestBindingException.class})
    public CommonResponse paramValidationExceptionHandle(Exception e) {
        CommonResponse commonResponse = CommonResponse.createCommonResponse();
        logger.error("[ParamValidationException]Exception:", e);
        return commonResponse.fail("Parameter validation failure! Message:" + e.getMessage());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(value = {ResourceNotFoundException.class})
    public CommonResponse resourceNotFoundExceptionHandle(Exception e) {
        CommonResponse commonResponse = CommonResponse.createCommonResponse();
        logger.error("[ResourceNotFoundException]Exception:", e);
        return commonResponse.fail("Resource not found! Message:" + e.getMessage());
    }*/

    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = Exception.class)
    public ReturnResult commonExceptionHandle(Exception e) {
        logger.error("[SystemException]Exception:", e);
        return ReturnResult.failure(StatusCode.SYSTEM_ERROR, "System error, please try again later! Message:" + e.getMessage());
    }
}