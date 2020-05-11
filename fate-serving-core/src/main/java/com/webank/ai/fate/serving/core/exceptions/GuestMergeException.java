package com.webank.ai.fate.serving.core.exceptions;

import com.webank.ai.fate.serving.core.constant.StatusCode;

public class GuestMergeException extends BaseException {
    public GuestMergeException(String msg) {
        super(StatusCode.GUEST_MERGE_ERROR, msg);
    }

    public GuestMergeException(String retCode, String message) {
        super(retCode, message);
    }
}
