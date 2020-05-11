package com.webank.ai.fate.serving.core.exceptions;


import com.webank.ai.fate.serving.core.constant.StatusCode;

public class RemoteRpcException extends BaseException {

    public RemoteRpcException(String retCode, String message) {
        super(retCode, message);
    }

    public RemoteRpcException(String message) {
        super(StatusCode.NET_ERROR, message);
    }
}
