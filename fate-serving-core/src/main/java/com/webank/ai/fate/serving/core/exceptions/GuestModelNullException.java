package com.webank.ai.fate.serving.core.exceptions;


import com.webank.ai.fate.serving.core.constant.StatusCode;

public class GuestModelNullException extends  BaseException{
    public GuestModelNullException(String retCode, String message) {
        super(retCode, message);
    }
    public GuestModelNullException(String message){
        super(StatusCode.GUEST_MODEL_NULL,message);
    }
}
