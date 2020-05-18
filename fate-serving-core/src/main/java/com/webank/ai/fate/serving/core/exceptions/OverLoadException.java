package com.webank.ai.fate.serving.core.exceptions;


import com.webank.ai.fate.serving.core.constant.StatusCode;

public class OverLoadException extends BaseException {

    public OverLoadException(String retCode, String message) {
        super(retCode, message);
    }

    public OverLoadException(String message) {
        super(StatusCode.OVER_LOAD_ERROR, message);
    }
}
