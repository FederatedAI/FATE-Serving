package com.webank.ai.fate.serving.core.exceptions;


public class RemoteRpcException  extends  BaseException{
    public RemoteRpcException(int retCode, String message) {
        super(retCode, message);
    }

    public RemoteRpcException( String message) {
        super(1115, message);
    }
}
