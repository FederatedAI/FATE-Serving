package com.webank.ai.fate.serving.monitor.handler;

import com.webank.ai.fate.serving.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.constant.StatusCode;
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
        ReturnResult result = new ReturnResult();
        result.setRetcode(StatusCode.PARAM_ERROR);
        result.setRetmsg("Parameter validation failure! Message:" + e.getMessage());
        return result;
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(AuthorizedException.class)
    public ReturnResult authorizedException(Exception e) {
        logger.error("[AuthorizedException]Exception:", e);
        ReturnResult result = new ReturnResult();
        result.setRetcode(StatusCode.UNAUTHORIZED);
        result.setRetmsg("User authorized failure! Message:" + e.getMessage());
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