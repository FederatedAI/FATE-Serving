package com.webank.ai.fate.serving.monitor.exceptions;

import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.exceptions.BaseException;

public class AuthorizedException extends BaseException {

    public AuthorizedException(String retCode, String message) {
        super(retCode, message);
    }

    public AuthorizedException(String message) {
        super(StatusCode.UNAUTHORIZED, message);
    }
}
