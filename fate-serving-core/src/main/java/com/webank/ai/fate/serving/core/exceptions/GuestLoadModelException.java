package com.webank.ai.fate.serving.core.exceptions;

import com.webank.ai.fate.serving.core.constant.StatusCode;

public class GuestLoadModelException extends BaseException  {

    public   GuestLoadModelException(String code,String msg){

        super(code,msg);

    }
    public   GuestLoadModelException(String msg){
        super(StatusCode.GUEST_LOAD_MODEL_ERROR,msg);
    }
}
