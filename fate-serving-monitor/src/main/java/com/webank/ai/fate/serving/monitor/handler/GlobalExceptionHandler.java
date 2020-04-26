package com.webank.ai.fate.serving.monitor.handler;

import com.webank.ai.fate.serving.monitor.bean.ReturnResult;
import com.webank.ai.fate.serving.monitor.bean.StatusCode;
import com.webank.ai.fate.serving.monitor.exceptions.AuthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * @Description Global exceptions management
 * @Date: 2020/3/25 11:11
 * @Author: v_dylanxu
 */
@RestControllerAdvice(annotations = ResponseBody.class)
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = {MethodArgumentTypeMismatchException.class, IllegalArgumentException.class})
    public ReturnResult paramValidationExceptionHandle(Exception e) {
        logger.error("[ParamValidationException]Exception:", e);
        return ReturnResult.failure(StatusCode.PARAM_ERROR, "Parameter validation failure! Message:" + e.getMessage());
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(AuthorizedException.class)
    public ReturnResult authorizedException(Exception e) {
        logger.error("[AuthorizedException]Exception:", e);
        return ReturnResult.failure(StatusCode.UNAUTHORIZED, "User authorized failure! Message:" + e.getMessage());
    }

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
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(value = {ResourceNotFoundException.class})
    public CommonResponse resourceNotFoundExceptionHandle(Exception e) {
        CommonResponse commonResponse = CommonResponse.createCommonResponse();
        logger.error("[ResourceNotFoundException]Exception:", e);
        return commonResponse.fail("Resource not found! Message:" + e.getMessage());
    }*/

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = Exception.class)
    public ReturnResult commonExceptionHandle(Exception e) {
        logger.error("[SystemException]Exception:", e);
        return ReturnResult.failure(HttpStatus.INTERNAL_SERVER_ERROR.value(), "System Error, please try again later! Message:" + e.getMessage());
    }
}