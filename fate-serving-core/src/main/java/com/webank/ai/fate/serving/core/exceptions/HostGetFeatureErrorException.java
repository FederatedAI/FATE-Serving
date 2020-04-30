package com.webank.ai.fate.serving.core.exceptions;


import com.webank.ai.fate.serving.core.constant.StatusCode;

public class HostGetFeatureErrorException  extends   BaseException{
    public HostGetFeatureErrorException( String message) {
        this(StatusCode.HOST_FEATURE_NOT_EXIST, message);
    }

    public HostGetFeatureErrorException(String retCode, String message) {
        super(retCode, message);

    }



}
