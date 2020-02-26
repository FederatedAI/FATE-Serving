package com.webank.ai.fate.serving.core.disruptor;

import com.lmax.disruptor.ExceptionHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DisruptorExceptionHandler implements ExceptionHandler {
    @Override
    public void handleEventException(Throwable ex, long sequence, Object event) {
        log.error("Disruptor event exception, {}", ex.getMessage());
        ex.printStackTrace();
    }

    @Override
    public void handleOnStartException(Throwable ex) {
        log.error("Disruptor start exception, {}", ex.getMessage());
        ex.printStackTrace();
    }

    @Override
    public void handleOnShutdownException(Throwable ex) {
        log.error("Disruptor shutdown exception, {}", ex.getMessage());
        ex.printStackTrace();
    }
}
