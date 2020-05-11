package com.webank.ai.fate.serving.core.exceptions;


import com.webank.ai.fate.serving.core.constant.StatusCode;

public class GuestInvalidParamException extends BaseException {
    public GuestInvalidParamException(String retCode, String message) {
        super(retCode, message);
    }

    public GuestInvalidParamException(String message) {
        super(StatusCode.GUEST_PARAM_ERROR, message);
    }


}
