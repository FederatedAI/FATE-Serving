package com.webank.ai.fate.serving.proxy.exceptions;


import com.webank.ai.fate.serving.proxy.common.ErrorMessageUtil;
import com.webank.ai.fate.serving.proxy.common.ResponseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

/**
 * 统一异常样处理类
 *
 * @Author
 */
@RestController
@ControllerAdvice
public class GlobalExceptionHandler {
    Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(Throwable.class)
    @ResponseBody
    public Map defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception {
        Map resultMap = ErrorMessageUtil.handleException(new HashMap(),e);
        return resultMap;
    }
}
