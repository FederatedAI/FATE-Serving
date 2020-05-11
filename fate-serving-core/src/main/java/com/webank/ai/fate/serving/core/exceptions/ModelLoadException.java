package com.webank.ai.fate.serving.core.exceptions;


import com.webank.ai.fate.serving.core.constant.StatusCode;

public class ModelLoadException extends BaseException {
    public ModelLoadException(String retCode, String message) {
        super(retCode, message);
    }

    public ModelLoadException(String message) {
        super(StatusCode.MODEL_NULL, message);
    }
}
