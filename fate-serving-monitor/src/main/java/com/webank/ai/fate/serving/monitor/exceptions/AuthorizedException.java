package com.webank.ai.fate.serving.monitor.exceptions;

import com.webank.ai.fate.serving.monitor.bean.StatusCode;

public class AuthorizedException extends BaseException {

    public AuthorizedException(int code, String message) {
        super(code, message);
    }

    public AuthorizedException(String message) {
        super(StatusCode.UNAUTHORIZED, message);
    }
}
