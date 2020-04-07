package com.webank.ai.fate.serving.core.exceptions;


import com.webank.ai.fate.serving.core.constant.StatusCode;

public class SysException extends BaseException {
    public SysException(String retCode, String message) {
        super(retCode, message);
    }

    public SysException(String message) {
        super(StatusCode.SYSTEM_ERROR, message);
    }
}
