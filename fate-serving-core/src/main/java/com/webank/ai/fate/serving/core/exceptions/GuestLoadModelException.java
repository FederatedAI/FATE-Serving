package com.webank.ai.fate.serving.core.exceptions;

/**
 * @Description TODO
 * @Author
 **/
public class GuestLoadModelException extends BaseException  {

    public GuestLoadModelException(){

        super(1104,"guest load model error");
    }

    public   GuestLoadModelException(String msg){
        super(1104,msg);
    }
}
