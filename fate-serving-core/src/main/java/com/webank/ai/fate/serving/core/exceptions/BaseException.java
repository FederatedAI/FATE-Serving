package com.webank.ai.fate.serving.core.exceptions;


public class BaseException extends RuntimeException {

    private int retCode;

    public BaseException(int retCode, String message) {
        super(message);
        this.retCode = retCode;
    }

    public int getRetCode() {
        return retCode;
    }
}
