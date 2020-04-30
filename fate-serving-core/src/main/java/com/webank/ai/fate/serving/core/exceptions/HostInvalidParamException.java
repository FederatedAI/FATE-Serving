package com.webank.ai.fate.serving.core.exceptions;


import com.webank.ai.fate.serving.core.constant.StatusCode;

public class HostInvalidParamException extends BaseException{
    public HostInvalidParamException(String retCode, String message) {
        super(retCode, message);
    }
    public HostInvalidParamException(String message) {
        super(StatusCode.HOST_PARAM_ERROR, message);
    }


}
