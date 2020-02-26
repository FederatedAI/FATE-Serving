package com.webank.ai.fate.serving.core.disruptor;

import com.lmax.disruptor.ExceptionHandler;

public class DisruptorExceptionHandler implements ExceptionHandler {
    @Override
    public void handleEventException(Throwable ex, long sequence, Object event) {
        // TODO: 2020/2/13
        ex.printStackTrace();
    }

    @Override
    public void handleOnStartException(Throwable ex) {
        // TODO: 2020/2/13
        ex.printStackTrace();
    }

    @Override
    public void handleOnShutdownException(Throwable ex) {
        // TODO: 2020/2/13
        ex.printStackTrace();
    }
}
