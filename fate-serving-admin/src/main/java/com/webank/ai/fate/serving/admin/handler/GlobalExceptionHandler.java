/*
 * Copyright 2019 The FATE Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.ai.fate.serving.admin.handler;

import com.webank.ai.fate.serving.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.exceptions.RemoteRpcException;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
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
        return ReturnResult.build(StatusCode.PARAM_ERROR, "request parameter error");
    }

    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(value = {StatusRuntimeException.class, RemoteRpcException.class})
    public ReturnResult statusRuntimeExceptionHandle(Exception e) {
        logger.error("[RemoteRpcException]Exception:", e);
        return ReturnResult.build(StatusCode.NET_ERROR, "remote rpc request exception");
    }

    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(value = Exception.class)
    public ReturnResult commonExceptionHandle(Exception e) {
        logger.error("[SystemException]Exception:", e);
        return ReturnResult.build(StatusCode.SYSTEM_ERROR, "system error, please check the log for detail");
    }

}