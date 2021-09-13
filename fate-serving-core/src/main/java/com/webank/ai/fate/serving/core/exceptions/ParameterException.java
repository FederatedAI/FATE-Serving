package com.webank.ai.fate.serving.core.exceptions;

import com.webank.ai.fate.serving.core.constant.StatusCode;

/**
 * @auther Xiongli
 * @date 2021/7/30
 * @remark
 */
public class ParameterException extends BaseException {

    public ParameterException(int retCode, String message) {
        super(retCode, message);
    }

    public ParameterException(String message) {
        super(StatusCode.OVER_LOAD_ERROR, message);
    }

}