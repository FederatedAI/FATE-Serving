package com.webank.ai.fate.serving.core.exceptions;


import com.webank.ai.fate.serving.core.constant.StatusCode;

public class HostModelNullException extends  BaseException{


    public HostModelNullException( String message) {
        super(StatusCode.HOST_MODEL_NULL, message);
    }

    public HostModelNullException(String retCode, String message) {
        super(retCode, message);
    }
}
