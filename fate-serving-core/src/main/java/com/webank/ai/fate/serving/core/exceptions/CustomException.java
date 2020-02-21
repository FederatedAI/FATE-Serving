package com.webank.ai.fate.serving.core.exceptions;


public class CustomException extends RuntimeException {

    private static final long serialVersionUID = 4564124491192825748L;

    private int retcode;

    public CustomException() {
        super();
    }

    public CustomException(int code, String message) {
        super(message);
        this.setCode(code);
    }

    public int getCode() {
        return retcode;
    }

    public void setCode(int code) {
        this.retcode = code;
    }
}