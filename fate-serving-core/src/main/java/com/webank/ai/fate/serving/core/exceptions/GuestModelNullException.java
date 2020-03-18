package com.webank.ai.fate.serving.core.exceptions;


public class GuestModelNullException extends  BaseException{
    public GuestModelNullException(int retCode, String message) {
        super(retCode, message);
    }

    public GuestModelNullException(String message){
        super(1104,message);
    }
}
