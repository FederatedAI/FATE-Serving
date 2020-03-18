package com.webank.ai.fate.serving.core.exceptions;


public class GuestInvalidParamException extends BaseException{
    public GuestInvalidParamException(int retCode, String message) {
        super(retCode, message);
    }
    public GuestInvalidParamException( String message) {
        super(1100, message);
    }


}
