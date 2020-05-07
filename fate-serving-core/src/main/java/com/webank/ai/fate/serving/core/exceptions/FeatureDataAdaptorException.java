package com.webank.ai.fate.serving.core.exceptions;


import com.webank.ai.fate.serving.core.constant.StatusCode;

public class FeatureDataAdaptorException extends BaseException {

    public FeatureDataAdaptorException(String message) {
        this(StatusCode.FEATURE_DATA_ADAPTOR_ERROR, message);
    }

    public FeatureDataAdaptorException(String retCode, String message) {
        super(retCode, message);
    }

}
