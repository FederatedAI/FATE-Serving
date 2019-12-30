package com.webank.ai.fate.serving.proxy.exceptions;

import org.springframework.core.NestedRuntimeException;

public class SysException extends NestedRuntimeException {

    public SysException(String msg) {
        super(msg);
    }

    public SysException(String msg, Throwable ex) {
        super(msg, ex);
    }
}
