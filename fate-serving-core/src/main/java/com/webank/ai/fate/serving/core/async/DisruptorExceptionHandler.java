package com.webank.ai.fate.serving.core.async;

import com.lmax.disruptor.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DisruptorExceptionHandler implements ExceptionHandler {

    private static Logger logger = LoggerFactory.getLogger(DisruptorExceptionHandler.class);

    @Override
    public void handleEventException(Throwable ex, long sequence, Object event) {
        logger.error("Disruptor event exception, {}", ex.getMessage());
        ex.printStackTrace();
    }

    @Override
    public void handleOnStartException(Throwable ex) {
        logger.error("Disruptor start exception, {}", ex.getMessage());
        ex.printStackTrace();
    }

    @Override
    public void handleOnShutdownException(Throwable ex) {
        logger.error("Disruptor shutdown exception, {}", ex.getMessage());
        ex.printStackTrace();
    }
}
