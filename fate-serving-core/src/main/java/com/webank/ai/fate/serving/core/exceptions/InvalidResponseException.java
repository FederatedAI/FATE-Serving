package com.webank.ai.fate.serving.core.exceptions;

import com.webank.ai.fate.serving.core.constant.StatusCode;

public class InvalidResponseException extends BaseException{

    public InvalidResponseException(String message) {
        super(StatusCode.INVALID_ROLE_ERROR, message);
    }


}
