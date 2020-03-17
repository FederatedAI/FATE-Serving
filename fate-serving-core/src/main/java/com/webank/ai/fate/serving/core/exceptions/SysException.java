package com.webank.ai.fate.serving.core.exceptions;



public class SysException extends BaseException {


    public SysException(int retCode, String message) {
        super(retCode, message);
    }
}
