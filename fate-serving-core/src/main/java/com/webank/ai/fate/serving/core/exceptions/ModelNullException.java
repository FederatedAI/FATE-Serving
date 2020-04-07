package com.webank.ai.fate.serving.core.exceptions;


import com.webank.ai.fate.serving.core.constant.StatusCode;

public class ModelNullException extends  BaseException{
    public ModelNullException(String retCode, String message) {
        super(retCode, message);
    }
    public ModelNullException(String message){
        super(StatusCode.MODEL_NULL,message);
    }
}
