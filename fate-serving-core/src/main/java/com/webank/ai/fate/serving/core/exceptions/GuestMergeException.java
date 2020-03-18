package com.webank.ai.fate.serving.core.exceptions;

public class GuestMergeException  extends BaseException{
    public GuestMergeException(int retCode, String message) {
        super(retCode, message);
    }
}
