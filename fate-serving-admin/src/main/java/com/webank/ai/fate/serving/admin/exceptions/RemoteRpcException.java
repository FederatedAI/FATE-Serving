package com.webank.ai.fate.serving.admin.exceptions;

import com.webank.ai.fate.serving.admin.bean.StatusCode;

public class RemoteRpcException extends BaseException {

    public RemoteRpcException(int code, String message) {
        super(code, message);
    }

    public RemoteRpcException(String message) {
        super(StatusCode.NET_ERROR, message);
    }
}
