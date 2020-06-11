package com.webank.ai.fate.serving.core.exceptions;


import com.webank.ai.fate.serving.core.constant.StatusCode;

public class ModelProcessorInitException extends BaseException {

    public ModelProcessorInitException(String message) {
        this(StatusCode.MODEL_INIT_ERROR, message);
    }

    public ModelProcessorInitException(String retCode, String message) {
        super(retCode, message);
    }

}
