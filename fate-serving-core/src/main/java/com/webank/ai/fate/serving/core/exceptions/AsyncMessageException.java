package com.webank.ai.fate.serving.core.exceptions;

public class AsyncMessageException extends RuntimeException {
    public AsyncMessageException(String message) {
        super(message);
    }
}
