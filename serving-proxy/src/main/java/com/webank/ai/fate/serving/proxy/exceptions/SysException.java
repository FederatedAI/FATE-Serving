package com.webank.ai.fate.serving.proxy.exceptions;

import org.springframework.core.NestedRuntimeException;

public class SysException extends NestedRuntimeException {
    /**
     * 构造函数。
     *
     * @param msg 异常描述
     */
    public SysException(String msg) {
        super(msg);
    }

    /**
     * 构造函数。
     *
     * @param msg 异常描述
     * @param ex  异常
     */
    public SysException(String msg, Throwable ex) {
        super(msg, ex);
    }
}
